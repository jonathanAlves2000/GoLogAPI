-- Migration: Passo 1 - Entidades Dinâmicas de Demanda, Capacidade, Perfil de Otimização e Morfologia
-- Database: PostgreSQL

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tabela de Tipos de Demanda (DemandType)
CREATE TABLE IF NOT EXISTS public.demand_type_table (
    id uuid PRIMARY KEY,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    unit character varying(50) NOT NULL,
    description text,
    is_system_default boolean NOT NULL DEFAULT false,
    company_id uuid REFERENCES public.company_table(id)
);

-- 2. Tabela de Demandas da Carga (ShipmentDemand)
CREATE TABLE IF NOT EXISTS public.shipment_demand_table (
    id uuid PRIMARY KEY,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    shipment_id uuid NOT NULL REFERENCES public.shipment_table(id) ON DELETE CASCADE,
    demand_type_id uuid NOT NULL REFERENCES public.demand_type_table(id),
    amount double precision NOT NULL
);

-- 3. Tabela de Capacidades do Equipamento (EquipamentCapacity)
CREATE TABLE IF NOT EXISTS public.equipament_capacity_table (
    id uuid PRIMARY KEY,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    equipament_id uuid NOT NULL REFERENCES public.equipament_table(id) ON DELETE CASCADE,
    demand_type_id uuid NOT NULL REFERENCES public.demand_type_table(id),
    max_capacity double precision NOT NULL,
    soft_max_capacity double precision,
    cost_per_unit_above_soft_max double precision
);

-- 4. Tabela de Perfis de Otimização e Custos (OptimizationProfile)
CREATE TABLE IF NOT EXISTS public.optimization_profile_table (
    id uuid PRIMARY KEY,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    company_id uuid NOT NULL REFERENCES public.company_table(id),
    name character varying(255) NOT NULL,
    description text,
    is_default boolean NOT NULL DEFAULT false,
    km_cost_multiplier double precision NOT NULL DEFAULT 1.0,
    hour_cost_multiplier double precision NOT NULL DEFAULT 1.0,
    fixed_cost_per_vehicle double precision NOT NULL DEFAULT 0.0,
    cost_per_traveled_hour double precision NOT NULL DEFAULT 0.0,
    penalty_cost_unserved double precision NOT NULL DEFAULT 100000.0,
    late_arrival_cost_per_hour double precision NOT NULL DEFAULT 0.0,
    default_service_duration_seconds integer NOT NULL DEFAULT 1800,
    time_window_lead_minutes integer NOT NULL DEFAULT 15,
    vehicle_start_window_lead_hours integer NOT NULL DEFAULT 2,
    vehicle_end_window_margin_hours integer NOT NULL DEFAULT 2,
    global_horizon_extra_days integer NOT NULL DEFAULT 2
);

-- 5. Tabela de Tipos de Visita / Morfologia (VisitType)
CREATE TABLE IF NOT EXISTS public.visit_type_table (
    id uuid PRIMARY KEY,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    company_id uuid REFERENCES public.company_table(id)
);

-- 6. Tabelas de Associação para Tipos de Visita
CREATE TABLE IF NOT EXISTS public.shipment_visit_type_table (
    shipment_id uuid NOT NULL REFERENCES public.shipment_table(id) ON DELETE CASCADE,
    visit_type_id uuid NOT NULL REFERENCES public.visit_type_table(id) ON DELETE CASCADE,
    PRIMARY KEY (shipment_id, visit_type_id)
);

CREATE TABLE IF NOT EXISTS public.equipament_group_visit_type_table (
    equipament_group_id uuid NOT NULL REFERENCES public.equipament_group_table(id) ON DELETE CASCADE,
    visit_type_id uuid NOT NULL REFERENCES public.visit_type_table(id) ON DELETE CASCADE,
    PRIMARY KEY (equipament_group_id, visit_type_id)
);

-- 7. Tabela de Regras de Morfologia / Compatibilidade (VisitTypeRule)
CREATE TABLE IF NOT EXISTS public.visit_type_rule_table (
    id uuid PRIMARY KEY,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    name character varying(255) NOT NULL,
    description text,
    rule_type character varying(50) NOT NULL,
    visit_type1_id uuid NOT NULL REFERENCES public.visit_type_table(id),
    visit_type2_id uuid NOT NULL REFERENCES public.visit_type_table(id),
    company_id uuid REFERENCES public.company_table(id)
);

-- Seeds padrão do sistema (Tipos de demanda fundamentais)
INSERT INTO public.demand_type_table (id, active, created_at, created_by, updated_at, updated_by, code, name, unit, description, is_system_default)
VALUES 
    (gen_random_uuid(), true, NOW(), 'System', NOW(), 'System', 'weight', 'Peso', 'kg', 'Demanda por peso da carga', true),
    (gen_random_uuid(), true, NOW(), 'System', NOW(), 'System', 'volume', 'Volume', 'm³', 'Demanda por volume cúbico', true),
    (gen_random_uuid(), true, NOW(), 'System', NOW(), 'System', 'pallets', 'Paletes', 'un', 'Quantidade de posições pallet', false)
ON CONFLICT DO NOTHING;
