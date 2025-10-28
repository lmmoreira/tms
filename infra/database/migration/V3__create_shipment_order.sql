CREATE TABLE shipmentorder.shipment_order (
      id UUID NOT NULL,
      is_archived BOOLEAN DEFAULT FALSE NOT NULL,
      company_id UUID NOT NULL,
      external_id VARCHAR(255) NOT NULL,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
      PRIMARY KEY (id, is_archived)
) PARTITION BY LIST (is_archived);

CREATE TABLE shipmentorder.shipment_order_active PARTITION OF shipmentorder.shipment_order FOR VALUES IN (false);
CREATE TABLE shipmentorder.shipment_order_archived PARTITION OF shipmentorder.shipment_order FOR VALUES IN (true);

CREATE INDEX idx_shipment_order_active_company_id ON shipmentorder.shipment_order_active(company_id);
CREATE INDEX idx_shipment_order_archived_company_id ON shipmentorder.shipment_order_archived(company_id);
