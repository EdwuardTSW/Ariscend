package com.ariscend.backend.service;

import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.TransactionCategory;
import com.ariscend.backend.repository.TransactionCategoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DefaultCategoryInitializer implements ApplicationRunner {

    private final TransactionCategoryRepository categoryRepository;

    public DefaultCategoryInitializer(TransactionCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createMissing(CategoryType.INCOME, List.of(
                new DefaultCategory("INCOME_SALARY", "Salario"),
                new DefaultCategory("INCOME_FREELANCE", "Trabajo independiente"),
                new DefaultCategory("INCOME_SALES", "Ventas"),
                new DefaultCategory("INCOME_INVESTMENTS", "Inversiones"),
                new DefaultCategory("INCOME_GIFTS", "Regalos"),
                new DefaultCategory("INCOME_OTHER", "Otros ingresos")
        ));
        createMissing(CategoryType.EXPENSE, List.of(
                new DefaultCategory("EXPENSE_FOOD", "Alimentación"),
                new DefaultCategory("EXPENSE_TRANSPORT", "Transporte"),
                new DefaultCategory("EXPENSE_HOUSING", "Vivienda"),
                new DefaultCategory("EXPENSE_SERVICES", "Servicios"),
                new DefaultCategory("EXPENSE_HEALTH", "Salud"),
                new DefaultCategory("EXPENSE_EDUCATION", "Educación"),
                new DefaultCategory("EXPENSE_ENTERTAINMENT", "Entretenimiento"),
                new DefaultCategory("EXPENSE_SHOPPING", "Compras"),
                new DefaultCategory("EXPENSE_GOALS", "Metas y ahorro"),
                new DefaultCategory("EXPENSE_OTHER", "Otros gastos")
        ));
    }

    private void createMissing(CategoryType type, List<DefaultCategory> categories) {
        for (DefaultCategory defaultCategory : categories) {
            List<TransactionCategory> matches = categoryRepository
                    .findAllBySystemDefinedTrueAndNameAndTypeOrderByIdAsc(
                            defaultCategory.name(),
                            type
                    );
            TransactionCategory category = categoryRepository.findBySystemKey(defaultCategory.key())
                    .orElseGet(() -> matches.isEmpty() ? new TransactionCategory() : matches.get(0));
            category.setName(defaultCategory.name());
            category.setType(type);
            category.setSystemDefined(true);
            category.setSystemKey(defaultCategory.key());
            category.setActive(true);
            categoryRepository.save(category);

            for (TransactionCategory duplicate : matches) {
                if (!duplicate.getId().equals(category.getId()) && duplicate.isActive()) {
                    duplicate.setActive(false);
                    categoryRepository.save(duplicate);
                }
            }
        }
    }

    private record DefaultCategory(String key, String name) {
    }
}
