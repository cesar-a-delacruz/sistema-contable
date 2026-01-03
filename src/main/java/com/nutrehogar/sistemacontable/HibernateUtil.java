package com.nutrehogar.sistemacontable;

import javax.swing.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.jetbrains.annotations.NotNull;

/**
 * HibernateUtil es una clase de utilidad que gestiona la configuración de
 * Hibernate
 * y proporciona una única instancia de SessionFactory y Session a lo largo de
 * la vida de la aplicación.
 * Esta clase utiliza el patrón Singleton para asegurar que solo haya una sesión
 * de Hibernate activa
 * en la aplicación de escritorio.
 * <p>
 * Se recomienda cerrar la sesión y el SessionFactory al finalizar el uso de la
 * aplicación.
 * </p>
 */
@Slf4j
public class HibernateUtil {
    private HibernateUtil() {
        throw new IllegalStateException("Utility class");
    }
    @NotNull
    @Getter
    private static final SessionFactory sessionFactory = buildSessionFactory();
    /**
     * Construye el SessionFactory utilizando la configuración especificada en
     * hibernate.cfg.xml.
     *
     * @return SessionFactory construida
     * @throws ExceptionInInitializerError
     *                                     si la configuración falla
     */
    private static SessionFactory buildSessionFactory() {
        log.info("Building Hibernate SessionFactory");
        try {
            return new HibernatePersistenceConfiguration("sistema_contable", Main.class)
                            .jdbcUrl("jdbc:hsqldb:file:./data/sistema_contable;shutdown=true")// "jdbc:sqlite:" + ConfigLoader.Props.DB_NAME.getPath().toString() + "?journal_mode=WAL"
                            .jdbcDriver("org.hsqldb.jdbcDriver")
                            .property("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
                            .property("hibernate.connection.provider_class", "agroal")
                            .jdbcPoolSize(1)
                            .schemaToolingAction(Action.NONE)
                            .showSql(true, true, true)
                            .createEntityManagerFactory();
        } catch (Exception e) {
            log.error("Error building SessionFactory", e);

            JOptionPane.showMessageDialog(null,
                    "Error al iniciar la sesión de Hibernate: " + e.getMessage(),
                    "Error de Configuración",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(1);
            return null;
        }
    }
    public static void shutdown() {
        sessionFactory.close();
    }
}
