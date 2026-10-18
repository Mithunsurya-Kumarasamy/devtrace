package com.devtrace.service;

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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryService.class);

    private final RepositoryEntityRepository repositoryRepo;
    private final GitHubApiClient gitHubApiClient;

    public RepositoryService(RepositoryEntityRepository repositoryRepo, GitHubApiClient gitHubApiClient) {
        this.repositoryRepo = repositoryRepo;
        this.gitHubApiClient = gitHubApiClient;
    }

    @Transactional
    public RepositoryResponse registerRepository(RepositoryRegistrationRequest request) {
        String fullName = request.getOwner() + "/" + request.getName();
        log.info("Registering repository: {}", fullName);

        // Fetch and validate on GitHub first
        GitHubRepoDto gitHubDto = gitHubApiClient.fetchRepository(request.getOwner(), request.getName());

        RepositoryEntity repoEntity = repositoryRepo.findByFullName(fullName)
                .orElseGet(() -> new RepositoryEntity(request.getOwner(), request.getName()));

        repoEntity.setDescription(gitHubDto.getDescription());
        repoEntity.setDefaultBranch(gitHubDto.getDefaultBranch() != null ? gitHubDto.getDefaultBranch() : "master");
        repoEntity.setPrivate(gitHubDto.isPrivate());
        repoEntity.setHtmlUrl(gitHubDto.getHtmlUrl());
        repoEntity.setGithubRepoId(gitHubDto.getId());
        repoEntity.setUpdatedAt(Instant.now());

        RepositoryEntity saved = repositoryRepo.save(repoEntity);
        log.info("Successfully registered repository: id={}, fullName={}", saved.getId(), saved.getFullName());
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
}
