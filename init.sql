--CREATE DATABASE ai_demo_db;

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(1024)
--    // 1536 for Open Ai
);