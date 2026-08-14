package com.ariscend.backend.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacyNoteContentMigration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyNoteContentMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public LegacyNoteContentMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Boolean usesLegacyBinaryType = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_schema = current_schema()
                      AND table_name = 'notes'
                      AND column_name = 'content'
                      AND data_type = 'bytea'
                )
                """, Boolean.class);

        if (Boolean.TRUE.equals(usesLegacyBinaryType)) {
            jdbcTemplate.execute("""
                    ALTER TABLE notes
                    ALTER COLUMN content TYPE text
                    USING convert_from(content, 'UTF8')
                    """);
            LOGGER.info("Migrated notes.content from bytea to text");
        }
    }
}
