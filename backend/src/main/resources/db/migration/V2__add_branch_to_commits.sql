-- V2: Add branch metadata to commits for branch-specific analytics
ALTER TABLE commits ADD COLUMN IF NOT EXISTS branch VARCHAR(100);
ALTER TABLE commits ADD COLUMN IF NOT EXISTS branches TEXT;
CREATE INDEX IF NOT EXISTS idx_commit_repo_branch ON commits(repository_id, branch);

UPDATE commits c
SET branch = (SELECT default_branch FROM repositories r WHERE r.id = c.repository_id)
WHERE c.branch IS NULL;

