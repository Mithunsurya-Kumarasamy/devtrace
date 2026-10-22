package com.devtrace.service.dependency;

import com.devtrace.dto.dependency.*;
import com.devtrace.entity.CommitFileEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.CommitFileEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DependencyGraphService {

    private static final Pattern JAVA_IMPORT = Pattern.compile("^[+]?\\s*import\\s+([a-zA-Z0-9_.]+);", Pattern.MULTILINE);
    private static final Pattern JS_IMPORT = Pattern.compile("^[+]?\\s*(?:import\\s+.*?\\s+from\\s+['\"]([^'\"]+)['\"]|require\\(['\"]([^'\"]+)['\"]\\))", Pattern.MULTILINE);
    private static final Pattern PY_IMPORT = Pattern.compile("^[+]?\\s*(?:from\\s+([a-zA-Z0-9_.]+)\\s+import|import\\s+([a-zA-Z0-9_.]+))", Pattern.MULTILINE);

    private final RepositoryEntityRepository repositoryRepo;
    private final CommitFileEntityRepository commitFileRepo;

    public DependencyGraphService(RepositoryEntityRepository repositoryRepo,
                                  CommitFileEntityRepository commitFileRepo) {
        this.repositoryRepo = repositoryRepo;
        this.commitFileRepo = commitFileRepo;
    }

    public DependencyGraphReportDto analyzeDependencyGraph(String repositoryId) {
        if (!repositoryRepo.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }

        List<CommitFileEntity> commitFiles = commitFileRepo.findByCommitRepositoryId(repositoryId);

        DependencyGraphReportDto report = new DependencyGraphReportDto();
        report.setRepositoryId(repositoryId);

        if (commitFiles.isEmpty()) {
            report.setTotalModules(0);
            report.setTotalDependencies(0);
            report.setHasCircularDependencies(false);
            report.setCircularDependenciesCount(0);
            report.setHighBlastRadiusCount(0);
            report.setFragileComponentsCount(0);
            report.setArchitecturalSummary("No source file modifications found to construct dependency graph.");
            return report;
        }

        // Track module statistics
        Map<String, ModuleAccumulator> moduleMap = new HashMap<>();
        // Directed dependency interactions: source -> (target -> weight)
        Map<String, Map<String, Integer>> dependencies = new HashMap<>();

        for (CommitFileEntity cf : commitFiles) {
            String filename = cf.getFilename();
            if (filename == null || filename.isBlank()) continue;

            String moduleName = extractModuleName(filename);
            String moduleType = detectModuleType(filename);

            ModuleAccumulator module = moduleMap.computeIfAbsent(moduleName, k -> new ModuleAccumulator(moduleName, moduleType));
            module.totalChurn += (cf.getAdditions() + cf.getDeletions());
            module.filesCount++;

            // Parse imports from rawPatch
            if (cf.getRawPatch() != null && !cf.getRawPatch().isBlank()) {
                Set<String> importedModules = parseImports(cf.getRawPatch(), filename);
                for (String targetModule : importedModules) {
                    if (!targetModule.equals(moduleName)) {
                        moduleMap.computeIfAbsent(targetModule, k -> new ModuleAccumulator(targetModule, moduleType));
                        dependencies.computeIfAbsent(moduleName, k -> new HashMap<>())
                                .merge(targetModule, 1, Integer::sum);
                    }
                }
            }
        }

        // Build JGraphT Directed Graph
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String mod : moduleMap.keySet()) {
            graph.addVertex(mod);
        }

        List<DependencyEdgeDto> edgeDtos = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : dependencies.entrySet()) {
            String source = entry.getKey();
            for (Map.Entry<String, Integer> targetEntry : entry.getValue().entrySet()) {
                String target = targetEntry.getKey();
                if (graph.containsVertex(source) && graph.containsVertex(target)) {
                    graph.addEdge(source, target);
                    edgeDtos.add(new DependencyEdgeDto(source, target, targetEntry.getValue()));
                }
            }
        }

        // Detect Circular Dependencies
        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        Set<String> cycleVertices = cycleDetector.findCycles();

        TarjanSimpleCycles<String, DefaultEdge> tarjan = new TarjanSimpleCycles<>(graph);
        List<List<String>> simpleCycles = tarjan.findSimpleCycles();

        List<CircularDependencyDto> circularDtos = new ArrayList<>();
        for (List<String> cycle : simpleCycles) {
            if (cycle.size() >= 2) {
                String rec = String.format("Circular dependency detected across %d modules: %s. Refactor by extracting common contracts or utilizing Dependency Inversion.",
                        cycle.size(), String.join(" -> ", cycle));
                circularDtos.add(new CircularDependencyDto(cycle.size(), cycle, rec));
            }
        }

        // Compute Coupling, Instability, and Overlay Hotspots
        List<DependencyNodeDto> nodeDtos = new ArrayList<>();
        int highBlastRadius = 0;
        int fragileComponents = 0;

        for (String mod : graph.vertexSet()) {
            ModuleAccumulator acc = moduleMap.get(mod);
            int inDegree = graph.inDegreeOf(mod);   // Ca (Afferent Coupling)
            int outDegree = graph.outDegreeOf(mod); // Ce (Efferent Coupling)

            double instability = 0.0;
            if (inDegree + outDegree > 0) {
                instability = round((double) outDegree / (inDegree + outDegree));
            }

            long churn = acc != null ? acc.totalChurn : 0;
            String hotspotLevel;
            if (churn >= 500) {
                hotspotLevel = "CRITICAL";
            } else if (churn >= 200) {
                hotspotLevel = "HIGH";
            } else if (churn >= 50) {
                hotspotLevel = "MEDIUM";
            } else {
                hotspotLevel = "LOW";
            }

            // Determine Architectural Role
            String role;
            if (inDegree >= 2 && ("CRITICAL".equals(hotspotLevel) || "HIGH".equals(hotspotLevel))) {
                role = "HIGH_BLAST_RADIUS";
                highBlastRadius++;
            } else if (instability >= 0.70 && ("CRITICAL".equals(hotspotLevel) || "HIGH".equals(hotspotLevel))) {
                role = "FRAGILE_VOLATILE";
                fragileComponents++;
            } else if (inDegree > 0 && instability <= 0.35) {
                role = "STABLE_CORE";
            } else {
                role = "NORMAL";
            }

            boolean inCycle = cycleVertices.contains(mod);

            nodeDtos.add(new DependencyNodeDto(
                    mod,
                    mod,
                    acc != null ? acc.type : "MODULE",
                    inDegree,
                    outDegree,
                    instability,
                    churn,
                    hotspotLevel,
                    role,
                    inCycle
            ));
        }

        nodeDtos.sort(Comparator.comparingInt(DependencyNodeDto::getAfferentCoupling).reversed());

        // Construct summary
        String summary = generateArchitecturalSummary(nodeDtos.size(), edgeDtos.size(),
                circularDtos.size(), highBlastRadius, fragileComponents);

        report.setTotalModules(nodeDtos.size());
        report.setTotalDependencies(edgeDtos.size());
        report.setHasCircularDependencies(!circularDtos.isEmpty());
        report.setCircularDependenciesCount(circularDtos.size());
        report.setHighBlastRadiusCount(highBlastRadius);
        report.setFragileComponentsCount(fragileComponents);
        report.setNodes(nodeDtos);
        report.setEdges(edgeDtos);
        report.setCircularDependencies(circularDtos);
        report.setArchitecturalSummary(summary);

        return report;
    }

    private String generateArchitecturalSummary(int totalMods, int totalDeps, int cycles, int blastRadius, int fragile) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Analyzed %d modules with %d inter-module dependencies. ", totalMods, totalDeps));

        if (cycles > 0) {
            sb.append(String.format("Found %d circular dependency cycle(s) violating acyclic architecture principles. ", cycles));
        } else {
            sb.append("Acyclic dependency structure verified (zero cycles detected). ");
        }

        if (blastRadius > 0) {
            sb.append(String.format("Identified %d HIGH_BLAST_RADIUS component(s) with high incoming dependencies and elevated churn volume; modifications warrant rigorous end-to-end regression testing. ", blastRadius));
        }

        if (fragile > 0) {
            sb.append(String.format("Detected %d FRAGILE_VOLATILE module(s) exhibiting high instability and recurrent churn. ", fragile));
        }

        if (cycles == 0 && blastRadius == 0 && fragile == 0) {
            sb.append("Codebase exhibits a stable, decoupled architectural hierarchy.");
        }

        return sb.toString().trim();
    }

    private String extractModuleName(String filename) {
        if (filename == null || filename.isBlank()) return "root";

        String normalized = filename.replace('\\', '/');

        // Java: backend/src/main/java/com/devtrace/...
        if (normalized.endsWith(".java")) {
            int javaIdx = normalized.indexOf("src/main/java/");
            if (javaIdx != -1) {
                String sub = normalized.substring(javaIdx + "src/main/java/".length());
                int lastSlash = sub.lastIndexOf('/');
                if (lastSlash != -1) {
                    return sub.substring(0, lastSlash).replace('/', '.');
                }
            }
        }

        // Generic directory extraction
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash > 0) {
            return normalized.substring(0, lastSlash);
        }
        return "root";
    }

    private String detectModuleType(String filename) {
        if (filename == null) return "MODULE";
        String lower = filename.replace('\\', '/').toLowerCase();
        if (lower.endsWith(".java")) return "JAVA";
        if (lower.endsWith(".ts") || lower.endsWith(".tsx")) return "TYPESCRIPT";
        if (lower.endsWith(".js") || lower.endsWith(".jsx")) return "JAVASCRIPT";
        if (lower.endsWith(".py")) return "PYTHON";
        return "MODULE";
    }

    private Set<String> parseImports(String rawPatch, String filename) {
        Set<String> imports = new HashSet<>();
        if (rawPatch == null || rawPatch.isBlank() || filename == null) return imports;

        String normalized = filename.replace('\\', '/').toLowerCase();

        if (normalized.endsWith(".java")) {
            Matcher matcher = JAVA_IMPORT.matcher(rawPatch);
            while (matcher.find()) {
                String fullImport = matcher.group(1);
                // Extract package (everything except class name)
                int lastDot = fullImport.lastIndexOf('.');
                if (lastDot > 0) {
                    String pkg = fullImport.substring(0, lastDot);
                    // Filter out standard java/javax/spring third-party packages to focus on internal modules
                    if (pkg.startsWith("com.devtrace") || (!pkg.startsWith("java.") && !pkg.startsWith("javax.") && !pkg.startsWith("org.springframework.") && !pkg.startsWith("jakarta."))) {
                        imports.add(pkg);
                    }
                }
            }
        } else if (normalized.endsWith(".ts") || normalized.endsWith(".tsx") || normalized.endsWith(".js") || normalized.endsWith(".jsx")) {
            Matcher matcher = JS_IMPORT.matcher(rawPatch);
            while (matcher.find()) {
                String imp = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (imp != null && (imp.startsWith(".") || imp.startsWith("@/"))) {
                    imports.add(cleanJsPath(imp, filename));
                }
            }
        } else if (normalized.endsWith(".py")) {
            Matcher matcher = PY_IMPORT.matcher(rawPatch);
            while (matcher.find()) {
                String imp = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (imp != null) {
                    imports.add(imp);
                }
            }
        }

        return imports;
    }

    private String cleanJsPath(String importPath, String currentFile) {
        // Normalize relative paths
        String currentDir = extractModuleName(currentFile);
        if (importPath.startsWith("./")) {
            return currentDir;
        } else if (importPath.startsWith("../")) {
            int lastSlash = currentDir.lastIndexOf('/');
            if (lastSlash > 0) {
                return currentDir.substring(0, lastSlash);
            }
            return "root";
        }
        return importPath;
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private static class ModuleAccumulator {
        final String name;
        final String type;
        long totalChurn = 0;
        int filesCount = 0;

        ModuleAccumulator(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}
