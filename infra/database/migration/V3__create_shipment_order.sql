CREATE TABLE shipment_order.shipment_order (
     id SERIAL,
     is_archived boolean default false,
     company_id UUID NOT NULL,
     external_id VARCHAR(255) NOT NULL,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
     PRIMARY KEY (id, is_archived)
) PARTITION BY LIST (is_archived);

CREATE TABLE shipment_order.shipment_order_active PARTITION OF shipment_order.shipment_order FOR VALUES IN (false);
CREATE TABLE shipment_order.shipment_order_archived PARTITION OF shipment_order.shipment_order FOR VALUES IN (true);

CREATE INDEX idx_shipment_order_active_company_id ON shipment_order.shipment_order_active(company_id);
CREATE INDEX idx_shipment_order_archived_company_id ON shipment_order.shipment_order_archived(company_id);
