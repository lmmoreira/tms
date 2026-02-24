package br.com.logistics.tms.company.domain;

import br.com.logistics.tms.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AgreementCondition Domain Tests")
class AgreementConditionTest {

    @Test
    @DisplayName("Should create valid agreement condition")
    void shouldCreateValidCondition() {
        final AgreementConditionId id = AgreementConditionId.unique();
        final Map<String, Object> conditionData = Map.of("percentage", 10.0);

        final AgreementCondition condition = new AgreementCondition(
                id,
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(conditionData)
        );

        assertThat(condition.agreementConditionId()).isEqualTo(id);
        assertThat(condition.conditionType()).isEqualTo(AgreementConditionType.DISCOUNT_PERCENTAGE);
        assertThat(condition.conditions()).isNotNull();
        assertThat(condition.conditions().value()).containsEntry("percentage", 10.0);
    }

    @Test
    @DisplayName("Should validate agreementConditionId is not null")
    void shouldValidateIdNotNull() {
        final Map<String, Object> conditionData = Map.of("percentage", 10.0);

        assertThatThrownBy(() -> new AgreementCondition(
                null,
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(conditionData)
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid agreementConditionId");
    }

    @Test
    @DisplayName("Should validate conditionType is not null")
    void shouldValidateTypeNotNull() {
        final AgreementConditionId id = AgreementConditionId.unique();
        final Map<String, Object> conditionData = Map.of("percentage", 10.0);

        assertThatThrownBy(() -> new AgreementCondition(
                id,
                null,
                Conditions.with(conditionData)
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid conditionType");
    }

    @Test
    @DisplayName("Should validate conditions are not null")
    void shouldValidateConditionsNotNull() {
        final AgreementConditionId id = AgreementConditionId.unique();

        assertThatThrownBy(() -> new AgreementCondition(
                id,
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                null
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid conditions");
    }

    @Test
    @DisplayName("Should support DISCOUNT_PERCENTAGE condition type")
    void shouldSupportDiscountPercentage() {
        final Map<String, Object> conditionData = Map.of("percentage", 15.5);

        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(conditionData)
        );

        assertThat(condition.conditionType()).isEqualTo(AgreementConditionType.DISCOUNT_PERCENTAGE);
        assertThat(condition.conditions().value()).containsEntry("percentage", 15.5);
    }

    @Test
    @DisplayName("Should support DELIVERY_SLA_DAYS condition type")
    void shouldSupportDeliverySLA() {
        final Map<String, Object> conditionData = Map.of("maxDays", 3);

        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DELIVERY_SLA_DAYS,
                Conditions.with(conditionData)
        );

        assertThat(condition.conditionType()).isEqualTo(AgreementConditionType.DELIVERY_SLA_DAYS);
        assertThat(condition.conditions().value()).containsEntry("maxDays", 3);
    }

    @Test
    @DisplayName("Should support complex condition data")
    void shouldSupportComplexConditionData() {
        final Map<String, Object> conditionData = Map.of(
                "percentage", 20.0,
                "minOrderValue", 1000.0,
                "region", "Southeast",
                "priority", "express"
        );

        final AgreementCondition condition = new AgreementCondition(
                AgreementConditionId.unique(),
                AgreementConditionType.DISCOUNT_PERCENTAGE,
                Conditions.with(conditionData)
        );

        assertThat(condition.conditions().value()).hasSize(4);
        assertThat(condition.conditions().value()).containsEntry("percentage", 20.0);
        assertThat(condition.conditions().value()).containsEntry("minOrderValue", 1000.0);
        assertThat(condition.conditions().value()).containsEntry("region", "Southeast");
        assertThat(condition.conditions().value()).containsEntry("priority", "express");
    }
}
