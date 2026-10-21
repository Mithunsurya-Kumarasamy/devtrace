package com.devtrace.service;

import com.devtrace.dto.GitHubBranchDto;
import com.devtrace.dto.GitHubRepoDto;
import com.devtrace.dto.RepositoryRegistrationRequest;
import com.devtrace.dto.RepositoryResponse;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.RepositoryEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryService.class);

    private final RepositoryEntityRepository repositoryRepo;
    private final GitHubApiClient gitHubApiClient;
    private final IngestionOrchestrator ingestionOrchestrator;

    public RepositoryService(
            RepositoryEntityRepository repositoryRepo,
            GitHubApiClient gitHubApiClient,
            IngestionOrchestrator ingestionOrchestrator) {
        this.repositoryRepo = repositoryRepo;
        this.gitHubApiClient = gitHubApiClient;
        this.ingestionOrchestrator = ingestionOrchestrator;
    }

    @Transactional
    public RepositoryResponse registerRepository(RepositoryRegistrationRequest request) {
        request.parseUrl();
        String owner = request.getOwner();
        String name = request.getName();
        String requestedBranch = request.getBranch();
        String fullName = owner + "/" + name;
        log.info("Registering repository: {} (requested branch: {})", fullName, requestedBranch);

        // Fetch and validate on GitHub first
        GitHubRepoDto gitHubDto = gitHubApiClient.fetchRepository(owner, name);

        RepositoryEntity repoEntity = repositoryRepo.findByFullName(fullName)
                .orElseGet(() -> new RepositoryEntity(owner, name));

        repoEntity.setDescription(gitHubDto.getDescription());
        if (requestedBranch != null && !requestedBranch.isBlank()) {
            repoEntity.setDefaultBranch(requestedBranch.trim());
        } else {
            repoEntity.setDefaultBranch(gitHubDto.getDefaultBranch() != null ? gitHubDto.getDefaultBranch() : "master");
        }
        repoEntity.setPrivate(gitHubDto.isPrivate());
        repoEntity.setHtmlUrl(gitHubDto.getHtmlUrl());
        repoEntity.setGithubRepoId(gitHubDto.getId());
        repoEntity.setUpdatedAt(Instant.now());

        RepositoryEntity saved = repositoryRepo.save(repoEntity);
        log.info("Successfully registered repository: id={}, fullName={}, defaultBranch={}",
                saved.getId(), saved.getFullName(), saved.getDefaultBranch());
        return RepositoryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> getAllRepositories() {
        return repositoryRepo.findAll().stream()
                .map(RepositoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepositoryById(String id) {
        RepositoryEntity repo = repositoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with ID " + id + " not found"));
        return RepositoryResponse.fromEntity(repo);
    }

    @Transactional(readOnly = true)
    public RepositoryEntity getEntityById(String id) {
        return repositoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with ID " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<String> getRepositoryBranches(String id) {
        RepositoryEntity repo = getEntityById(id);
        List<GitHubBranchDto> branchDtos = gitHubApiClient.fetchBranches(repo.getOwner(), repo.getName());
        List<String> branchNames = new ArrayList<>();
        if (branchDtos != null) {
            for (GitHubBranchDto b : branchDtos) {
                if (b.getName() != null && !b.getName().isBlank()) {
                    branchNames.add(b.getName());
                }
            }
        }
        String currentBranch = repo.getDefaultBranch() != null ? repo.getDefaultBranch() : "master";
        if (!branchNames.contains(currentBranch)) {
            branchNames.add(0, currentBranch);
        }
        return branchNames;
    }

    @Transactional
    public RepositoryResponse updateRepositoryBranch(String id, String newBranch) {
        if (newBranch == null || newBranch.isBlank()) {
            throw new IllegalArgumentException("Branch name must not be blank");
        }
        RepositoryEntity repo = getEntityById(id);
        String trimmed = newBranch.trim();
        if (!trimmed.equals(repo.getDefaultBranch())) {
            log.info("Switching branch for repository {} from '{}' to '{}'", repo.getFullName(), repo.getDefaultBranch(), trimmed);
            repo.setDefaultBranch(trimmed);
            repo.setUpdatedAt(Instant.now());
            ingestionOrchestrator.resetCheckpoint(repo, "COMMITS");
            repo = repositoryRepo.save(repo);
        }
        return RepositoryResponse.fromEntity(repo);
    }
}
