package br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities;

import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.infrastructure.config.ShipmentOrderSchema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "company", schema = ShipmentOrderSchema.SHIPMENT_ORDER_SCHEMA)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentOrderCompanyEntity implements Serializable {

    @Id
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false)
    private Map<String, Object> data;

    public static ShipmentOrderCompanyEntity from(final Company company) {
        return new ShipmentOrderCompanyEntity(
                company.getCompanyId().value(),
                company.getData().value()
        );
    }

    public Company toDomain() {
        return Company.createCompany(
                this.companyId,
                this.data != null ? new HashMap<>(this.data) : new HashMap<>()
        );
    }
}
