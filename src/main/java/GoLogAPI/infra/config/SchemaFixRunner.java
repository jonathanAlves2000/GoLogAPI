package GoLogAPI.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ajustes de schema que o {@code ddl-auto=update} do Hibernate não faz:
 * ele cria colunas novas, mas nunca altera o tipo de uma coluna existente.
 *
 * <p>Sem isto, bancos criados antes da troca de {@code attachment} para TEXT
 * continuam com varchar(255) e todo POST /occurrence com assinatura SVG falha
 * com "value too long" (409). Equivale à migration_step10, mas roda sozinho
 * em cada start e é idempotente.
 */
@Component
public class SchemaFixRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaFixRunner.class);

    private final JdbcTemplate jdbc;

    public SchemaFixRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureText("occurrence_table", "attachment");
    }

    private void ensureText(String table, String column) {
        try {
            String type = jdbc.query(
                    "SELECT data_type FROM information_schema.columns "
                            + "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    table, column);
            if (type == null || type.equalsIgnoreCase("text")) return;

            jdbc.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " TYPE TEXT");
            log.info("Coluna {}.{} convertida de {} para TEXT", table, column, type);
        } catch (Exception e) {
            // Não impede a API de subir; o erro fica no log para correção manual.
            log.error("Falha ao converter {}.{} para TEXT", table, column, e);
        }
    }
}
