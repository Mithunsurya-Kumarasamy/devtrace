-- ==============================================================================
-- DevTrace — Software Engineering Intelligence Platform
-- Migration: V1 Initial Schema
-- ==============================================================================

-- 1. Repositories
CREATE TABLE IF NOT EXISTS repositories (
    id VARCHAR(36) PRIMARY KEY,
    owner VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    full_name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    default_branch VARCHAR(100) DEFAULT 'master',
    is_private BOOLEAN DEFAULT FALSE,
    html_url VARCHAR(500),
    github_repo_id BIGINT,
    sync_status VARCHAR(50) DEFAULT 'IDLE',
    last_synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_repo_owner_name ON repositories(owner, name);
CREATE INDEX idx_repo_sync_status ON repositories(sync_status);

-- 2. Synchronization Jobs
CREATE TABLE IF NOT EXISTS sync_jobs (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(100),
    progress_percentage INTEGER DEFAULT 0,
    items_processed INTEGER DEFAULT 0,
    total_items_estimated INTEGER DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_sync_job_repo_status ON sync_jobs(repository_id, status);

-- 3. Synchronization Checkpoints (Incremental ingestion)
CREATE TABLE IF NOT EXISTS sync_checkpoints (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    resource_type VARCHAR(50) NOT NULL,
    last_sync_timestamp TIMESTAMP WITH TIME ZONE,
    last_external_id VARCHAR(100),
    checkpoint_data TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_repo_resource UNIQUE (repository_id, resource_type)
);

-- 4. Commits
CREATE TABLE IF NOT EXISTS commits (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    sha VARCHAR(40) NOT NULL,
    author_name VARCHAR(150),
    author_email VARCHAR(150),
    author_date TIMESTAMP WITH TIME ZONE,
    committer_name VARCHAR(150),
    committer_email VARCHAR(150),
    commit_date TIMESTAMP WITH TIME ZONE NOT NULL,
    message TEXT NOT NULL,
    parent_shas TEXT,
    additions INTEGER DEFAULT 0,
    deletions INTEGER DEFAULT 0,
    total_changes INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_repo_commit_sha UNIQUE (repository_id, sha)
);

CREATE INDEX idx_commit_repo_date ON commits(repository_id, commit_date DESC);
CREATE INDEX idx_commit_author_email ON commits(author_email);

-- 5. Commit Files (File-level churn tracking)
CREATE TABLE IF NOT EXISTS commit_files (
    id VARCHAR(36) PRIMARY KEY,
    commit_id VARCHAR(36) NOT NULL REFERENCES commits(id) ON DELETE CASCADE,
    filename VARCHAR(1000) NOT NULL,
    status VARCHAR(50),
    additions INTEGER DEFAULT 0,
    deletions INTEGER DEFAULT 0,
    changes INTEGER DEFAULT 0,
    raw_patch TEXT
);

CREATE INDEX idx_commit_file_filename ON commit_files(filename);
CREATE INDEX idx_commit_file_commit_id ON commit_files(commit_id);

-- 6. Pull Requests
CREATE TABLE IF NOT EXISTS pull_requests (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    pr_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT,
    state VARCHAR(50) NOT NULL,
    author_login VARCHAR(100) NOT NULL,
    author_avatar_url VARCHAR(500),
    base_branch VARCHAR(100),
    head_branch VARCHAR(100),
    additions INTEGER DEFAULT 0,
    deletions INTEGER DEFAULT 0,
    changed_files INTEGER DEFAULT 0,
    is_draft BOOLEAN DEFAULT FALSE,
    is_merged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    merged_at TIMESTAMP WITH TIME ZONE,
    first_review_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_repo_pr_number UNIQUE (repository_id, pr_number)
);

CREATE INDEX idx_pr_repo_state ON pull_requests(repository_id, state);
CREATE INDEX idx_pr_author ON pull_requests(author_login);
CREATE INDEX idx_pr_created_at ON pull_requests(created_at DESC);

-- 7. Pull Request Reviews
CREATE TABLE IF NOT EXISTS pr_reviews (
    id VARCHAR(36) PRIMARY KEY,
    pull_request_id VARCHAR(36) NOT NULL REFERENCES pull_requests(id) ON DELETE CASCADE,
    github_review_id BIGINT,
    reviewer_login VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    body TEXT
);

CREATE INDEX idx_pr_review_pr ON pr_reviews(pull_request_id);
CREATE INDEX idx_pr_review_reviewer ON pr_reviews(reviewer_login);

-- 8. Pull Request Events (Lifecycle timeline reconstruction)
CREATE TABLE IF NOT EXISTS pr_timeline_events (
    id VARCHAR(36) PRIMARY KEY,
    pull_request_id VARCHAR(36) NOT NULL REFERENCES pull_requests(id) ON DELETE CASCADE,
    actor_login VARCHAR(100),
    event_type VARCHAR(50) NOT NULL,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    details TEXT
);

CREATE INDEX idx_pr_event_pr_time ON pr_timeline_events(pull_request_id, event_timestamp ASC);

-- 9. Issues
CREATE TABLE IF NOT EXISTS issues (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    issue_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT,
    state VARCHAR(50) NOT NULL,
    author_login VARCHAR(100) NOT NULL,
    labels TEXT,
    comments_count INTEGER DEFAULT 0,
    category VARCHAR(50) DEFAULT 'OTHER',
    category_confidence DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_repo_issue_number UNIQUE (repository_id, issue_number)
);

CREATE INDEX idx_issue_repo_state ON issues(repository_id, state);
CREATE INDEX idx_issue_category ON issues(category);
CREATE INDEX idx_issue_created ON issues(created_at DESC);

-- 10. Releases
CREATE TABLE IF NOT EXISTS releases (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    tag_name VARCHAR(100) NOT NULL,
    name VARCHAR(255),
    body TEXT,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_draft BOOLEAN DEFAULT FALSE,
    is_prerelease BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_repo_tag UNIQUE (repository_id, tag_name)
);

CREATE INDEX idx_release_repo_pub ON releases(repository_id, published_at DESC);

-- 11. Anomaly Records
CREATE TABLE IF NOT EXISTS anomaly_records (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    metric_name VARCHAR(100) NOT NULL,
    observed_value DOUBLE PRECISION NOT NULL,
    expected_lower DOUBLE PRECISION NOT NULL,
    expected_upper DOUBLE PRECISION NOT NULL,
    baseline_mean DOUBLE PRECISION,
    severity VARCHAR(20) NOT NULL,
    detection_method VARCHAR(50) NOT NULL,
    explanation TEXT NOT NULL,
    detection_timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_anomaly_repo_time ON anomaly_records(repository_id, detection_timestamp DESC);
CREATE INDEX idx_anomaly_severity ON anomaly_records(severity);

-- 12. Pull Request Risk Predictions
CREATE TABLE IF NOT EXISTS pr_risk_predictions (
    id VARCHAR(36) PRIMARY KEY,
    pull_request_id VARCHAR(36) NOT NULL REFERENCES pull_requests(id) ON DELETE CASCADE,
    risk_score DOUBLE PRECISION NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    confidence DOUBLE PRECISION,
    contributing_factors TEXT,
    model_version VARCHAR(50) NOT NULL,
    is_heuristic BOOLEAN DEFAULT TRUE,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_pr_risk UNIQUE (pull_request_id)
);

CREATE INDEX idx_pr_risk_level ON pr_risk_predictions(risk_level);
