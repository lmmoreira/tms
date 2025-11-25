package br.com.logistics.tms.shipmentorder.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.domain.Status;
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

    @Column(name = "status", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'A'")
    private Character status;

    public static ShipmentOrderCompanyEntity of(final Company company) {
        return new ShipmentOrderCompanyEntity(
                company.getCompanyId().value(),
                company.getData().value(),
                company.getStatus().value()
        );
    }

    public Company toDomain() {
        final Company company = Company.createCompany(
                this.companyId,
                this.data != null ? new HashMap<>(this.data) : new HashMap<>()
        );
        return company.updateStatus(Status.of(this.status));
    }
}
