--
-- PostgreSQL database dump
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Dumped from database version 18.0 (Debian 18.0-1.pgdg13+3)
-- Dumped by pg_dump version 18.1

-- Started on 2026-06-05 02:11:52 UTC

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 219 (class 1259 OID 16385)
-- Name: address_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.address_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    cep character varying(255),
    city character varying(255),
    complement character varying(255),
    country character varying(255),
    district character varying(255),
    number character varying(255),
    state character varying(255),
    street character varying(255),
    latitude character varying(255),
    longitude character varying(255)
);


ALTER TABLE public.address_table OWNER TO admin;

--
-- TOC entry 234 (class 1259 OID 16809)
-- Name: collect_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.collect_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    sequence integer NOT NULL,
    address_id uuid NOT NULL,
    customer_collects_id uuid NOT NULL,
    status character varying(255),
    collection_schedule timestamp with time zone,
    route_planned text,
    route_completed text
);


ALTER TABLE public.collect_table OWNER TO admin;

--
-- TOC entry 220 (class 1259 OID 16394)
-- Name: company_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.company_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    cnpj_cpf character varying(255),
    email character varying(255),
    is_cliente boolean,
    legal_name character varying(255),
    phone_number character varying(255),
    address_id uuid
);


ALTER TABLE public.company_table OWNER TO admin;

--
-- TOC entry 221 (class 1259 OID 16403)
-- Name: driver_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.driver_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    cnh_expiration date,
    cnh_number character varying(255),
    user_id uuid
);


ALTER TABLE public.driver_table OWNER TO admin;

--
-- TOC entry 222 (class 1259 OID 16412)
-- Name: equipament_group_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.equipament_group_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    observation character varying(255),
    equipament1_id uuid NOT NULL,
    equipament2_id uuid,
    equipament3_id uuid
);


ALTER TABLE public.equipament_group_table OWNER TO admin;

--
-- TOC entry 223 (class 1259 OID 16422)
-- Name: equipament_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.equipament_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    maximum_capacity double precision,
    model character varying(255),
    number_axles integer,
    plate character varying(255),
    renavam character varying(255),
    company_id uuid
);


ALTER TABLE public.equipament_table OWNER TO admin;

--
-- TOC entry 227 (class 1259 OID 16561)
-- Name: group_transport_type_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.group_transport_type_table (
    equipament_group_id uuid NOT NULL,
    type_transport_id uuid NOT NULL
);


ALTER TABLE public.group_transport_type_table OWNER TO admin;

--
-- TOC entry 232 (class 1259 OID 16762)
-- Name: occurrence_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.occurrence_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    attachment character varying(255),
    description character varying(255),
    type character varying(255) NOT NULL,
    delivery_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    transport_id uuid NOT NULL
);


ALTER TABLE public.occurrence_table OWNER TO admin;

--
-- TOC entry 230 (class 1259 OID 16622)
-- Name: shipment_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.shipment_table (
    id uuid CONSTRAINT delivery_table_id_not_null NOT NULL,
    active boolean CONSTRAINT delivery_table_active_not_null NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    shipping_sequence character varying(255),
    schedulind timestamp(6) without time zone,
    status character varying(255),
    volume double precision,
    weight double precision,
    shipment_customer_id uuid,
    shipment_address_id uuid,
    transport_id uuid,
    shipment_type_id uuid,
    type_transport_id uuid,
    responsible_id uuid,
    type_operation character varying(255),
    carga_origem_id uuid,
    route_planned character varying(255),
    route_completed character varying(255),
    calculated_distance double precision,
    calculated_duration double precision,
    calculated_wait double precision,
    calculated_cost double precision
);


ALTER TABLE public.shipment_table OWNER TO admin;

--
-- TOC entry 231 (class 1259 OID 16685)
-- Name: shipment_type; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.shipment_type (
    id uuid CONSTRAINT delivery_type_id_not_null NOT NULL,
    active boolean CONSTRAINT delivery_type_active_not_null NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    care character varying(255),
    description character varying(255),
    name character varying(255)
);


ALTER TABLE public.shipment_type OWNER TO admin;

--
-- TOC entry 233 (class 1259 OID 16790)
-- Name: telemetry_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.telemetry_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    alert character varying(255),
    data1 character varying(255),
    data2 character varying(255),
    date_time timestamp(6) without time zone NOT NULL,
    device character varying(255),
    latitude character varying(255) NOT NULL,
    longitude character varying(255) NOT NULL,
    speed double precision NOT NULL,
    equipament_id uuid NOT NULL
);


ALTER TABLE public.telemetry_table OWNER TO admin;

--
-- TOC entry 224 (class 1259 OID 16431)
-- Name: tractor_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.tractor_table (
    type_fuel character varying(255),
    id uuid NOT NULL,
    km_per_liter double precision,
    co2_per_km double precision DEFAULT 0.0
);


ALTER TABLE public.tractor_table OWNER TO admin;

--
-- TOC entry 225 (class 1259 OID 16437)
-- Name: trailer_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.trailer_table (
    maximum_volume double precision,
    id uuid NOT NULL
);


ALTER TABLE public.trailer_table OWNER TO admin;

--
-- TOC entry 229 (class 1259 OID 16590)
-- Name: transport_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.transport_table (
    id uuid NOT NULL,
    shipment_quantity integer CONSTRAINT transport_table_delivery_quantity_not_null NOT NULL,
    route_return_planned character varying(255),
    route_return_completed character varying(255),
    time_stopped integer,
    distance_traveled integer DEFAULT 0,
    total_time integer,
    driver_id uuid,
    equipament_group_id uuid NOT NULL,
    transporter_id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    active boolean DEFAULT true,
    route_planned character varying(255),
    route_completed character varying(255),
    calculed_distance integer,
    time_stopped_calculed integer,
    total_time_calculed integer,
    total_cost_calculated double precision,
    total_cost double precision,
    total_cost_calculed double precision,
    code_transport integer NOT NULL
);


ALTER TABLE public.transport_table OWNER TO admin;

--
-- TOC entry 235 (class 1259 OID 16905)
-- Name: transport_table_code_transport_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.transport_table_code_transport_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transport_table_code_transport_seq OWNER TO admin;

--
-- TOC entry 3586 (class 0 OID 0)
-- Dependencies: 235
-- Name: transport_table_code_transport_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.transport_table_code_transport_seq OWNED BY public.transport_table.code_transport;


--
-- TOC entry 228 (class 1259 OID 16568)
-- Name: type_transport_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.type_transport_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    care character varying(255) NOT NULL,
    description character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.type_transport_table OWNER TO admin;

--
-- TOC entry 226 (class 1259 OID 16443)
-- Name: user_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.user_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    cpf character varying(255),
    email character varying(255),
    name character varying(255),
    password character varying(255),
    user_profile character varying(255),
    company_id uuid,
    CONSTRAINT user_table_user_profile_check CHECK (((user_profile)::text = ANY ((ARRAY['ADMIN'::character varying, 'DRIVER'::character varying, 'OPERATOR'::character varying])::text[])))
);


ALTER TABLE public.user_table OWNER TO admin;

--
-- TOC entry 3352 (class 2604 OID 16906)
-- Name: transport_table code_transport; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table ALTER COLUMN code_transport SET DEFAULT nextval('public.transport_table_code_transport_seq'::regclass);


--
-- TOC entry 3564 (class 0 OID 16385)
-- Dependencies: 219
-- Data for Name: address_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.address_table (id, active, created_at, created_by, updated_at, updated_by, cep, city, complement, country, district, number, state, street, latitude, longitude) FROM stdin;
ace29b92-0e59-437a-b7f6-de8c2fa772c5	t	2026-05-24 16:18:44.409603+00	admin@admin.com	2026-05-31 17:07:50.407819+00	admin@admin.com	13609390	Araras	Lactales	Brasil	Bosque dos Versalles	655	SP	Av Ângelo Franzini	-22.377172828853702	-47.3824185135542
550e8400-e29b-41d4-a716-446655440000	t	\N	\N	2026-06-01 12:29:30.647343+00	admin@admin.com	13601020	Araras	Fabrica de Cafe	Brasil	Jardim Belvedere	929	SP	Av Zurita	-22.365958873162253	-47.38092631046195
6e51b327-bec5-48eb-b881-76daf766ea1e	t	2026-05-03 15:38:33.274998+00	admin@admin.com	2026-06-01 12:31:35.619095+00	admin@admin.com	13607300	Araras	Mercado Faveta	Brasil	Do Campinho	1830	SP	Av Melvin Jones	-22.368117059390062	-47.36234857027745
1f2f0764-3e1c-4665-aefa-daa1da658564	t	2026-06-01 12:33:47.862716+00	admin@admin.com	2026-06-01 12:33:47.862716+00	admin@admin.com	13606831	Araras	Mercado Copacabana	Brasil	Parque Dom Pedro	243	SP	R Leopoldo Mazon Neto	-22.348362944866246	-47.3271826510571
1e58d64a-16bc-421d-b671-e0a52cb440a4	t	2026-06-01 12:36:04.828732+00	admin@admin.com	2026-06-01 12:36:04.828732+00	admin@admin.com	13606831	Araras	Mercado Pague Menos	Brasil	Vila Michelin	1075	SP	Av Dona Renata	-22.34759560971791	-47.38447581831087
dee697ea-e4d0-412b-84fc-c18cf79cbd3f	t	2026-06-01 13:39:04.887988+00	admin@admin.com	2026-06-01 13:39:04.887988+00	admin@admin.com	13607355	Araras	Transportador	Brasil	Jardim Ouro Verde	437	SP	Rua Maria Pascuotte Moraes	-22.379374549904945	-47.3635780333836
\.


--
-- TOC entry 3579 (class 0 OID 16809)
-- Dependencies: 234
-- Data for Name: collect_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.collect_table (id, active, created_at, created_by, updated_at, updated_by, sequence, address_id, customer_collects_id, status, collection_schedule, route_planned, route_completed) FROM stdin;
9b8be550-57f6-4938-a1f8-f61f6c88cb05	t	2026-05-24 17:13:16.994212+00	admin@admin.com	2026-05-31 17:26:04.438486+00	admin@admin.com	1	ace29b92-0e59-437a-b7f6-de8c2fa772c5	af29a1ab-407a-497d-b721-f3b93450d9eb	EM TRANSITO	2026-06-11 13:50:30+00	-22.377182749785067, -47.382418513380095	-22.377182749785067, -47.382418513380095
\.


--
-- TOC entry 3565 (class 0 OID 16394)
-- Dependencies: 220
-- Data for Name: company_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.company_table (id, active, created_at, created_by, updated_at, updated_by, cnpj_cpf, email, is_cliente, legal_name, phone_number, address_id) FROM stdin;
af29a1ab-407a-497d-b721-f3b93450d9eb	t	2026-05-24 16:30:51.682444+00	admin@admin.com	2026-05-24 16:30:51.682444+00	admin@admin.com	05300331003267	lactalis@lactalis.com.br	t	Lactalis Brasil	(19)35434500	ace29b92-0e59-437a-b7f6-de8c2fa772c5
d9d7b435-c256-405b-877c-848f4a22e22a	t	2026-03-14 23:30:52.035627+00	\N	2026-06-01 13:24:48.009591+00	admin@admin.com	60409075000667	nestle@gmail.com	t	Nestlé Brasil	(19)35431100	550e8400-e29b-41d4-a716-446655440000
a92a50c7-b904-4637-b7ca-0f794ad50eaa	t	2026-05-03 15:59:55.55528+00	admin@admin.com	2026-06-01 13:28:17.346877+00	admin@admin.com	44809564000145	faveta@gmail.com	t	Faveta Brasil	(19)35442999	6e51b327-bec5-48eb-b881-76daf766ea1e
ab393f44-c83b-485e-b152-d6403e41163e	t	2026-06-01 13:31:12.420737+00	admin@admin.com	2026-06-01 13:31:12.420737+00	admin@admin.com	54420625000131	copacaba@lactalis.com.br	t	Copacabana Supermercados	(19)35432870	1f2f0764-3e1c-4665-aefa-daa1da658564
eb97a8b5-a5ae-4985-a4d7-dbf30eefe347	t	2026-06-01 13:34:13.943188+00	admin@admin.com	2026-06-01 13:34:13.943188+00	admin@admin.com	60494416002502	pague@lactalis.com.br	t	Supermercado Pague Menos	(19)35080360	1e58d64a-16bc-421d-b671-e0a52cb440a4
7f564f96-d90f-42cc-beb2-e37cf63a324d	t	2026-06-01 13:39:36.345923+00	admin@admin.com	2026-06-01 13:39:36.345923+00	admin@admin.com	49018326895	golog@gmail.com	f	GoLogTransportes	(19)983282551	dee697ea-e4d0-412b-84fc-c18cf79cbd3f
\.


--
-- TOC entry 3566 (class 0 OID 16403)
-- Dependencies: 221
-- Data for Name: driver_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.driver_table (id, active, created_at, created_by, updated_at, updated_by, cnh_expiration, cnh_number, user_id) FROM stdin;
0fa0d515-0c4b-47be-bfd6-ef042669c0a0	t	2026-03-15 23:05:41.222869+00	Admin Master	2026-03-15 23:05:41.222869+00	Admin Master	2030-10-10	85983282551	438eca76-a0cf-480b-8c85-f129ec3e9216
\.


--
-- TOC entry 3567 (class 0 OID 16412)
-- Dependencies: 222
-- Data for Name: equipament_group_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_group_table (id, active, created_at, created_by, updated_at, updated_by, observation, equipament1_id, equipament2_id, equipament3_id) FROM stdin;
07796d8e-bacb-40f9-beb8-c02b6d00ff9d	t	2026-04-08 23:01:26.888952+00	Admin Master	2026-06-01 12:58:48.170686+00	admin@admin.com	Conjunto 1	60380c2a-025b-426f-b922-32c82f5209f4	\N	\N
9df01abb-f14a-49ef-955d-2d87185f8a51	t	2026-04-08 23:21:24.827769+00	Admin Master	2026-06-01 12:59:52.238563+00	admin@admin.com	Conjunto 2	bcf58715-e5ea-4e31-b356-d9b1abf6631c	01484413-6789-40b1-8d95-af53b235fc8b	\N
d681c272-2657-4800-a298-9310aafdceda	t	2026-04-08 23:15:26.413148+00	Admin Master	2026-06-01 13:00:56.568358+00	admin@admin.com	Conjunto 3	614b9420-d569-4f8f-a63d-e260752db17c	dec0b3c8-4302-4aa7-a78b-3281e6337602	\N
d5820f33-c8d2-44a7-90b9-8914d127e77a	t	2026-04-08 23:14:57.914549+00	Admin Master	2026-06-01 13:03:02.604983+00	admin@admin.com	Conjunto 4	dc4f5f09-1d69-4838-96bf-9e81639676f2	\N	\N
e56df2ca-a445-4e1e-ab3b-d1ba5bb9192b	t	2026-06-04 12:22:44.086062+00	admin@admin.com	2026-06-04 12:22:44.086062+00	admin@admin.com	Conjunct 5	35f4e73d-bb9e-43d3-b4c3-3220c5dac7e7	\N	\N
\.


--
-- TOC entry 3568 (class 0 OID 16422)
-- Dependencies: 223
-- Data for Name: equipament_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_table (id, active, created_at, created_by, updated_at, updated_by, maximum_capacity, model, number_axles, plate, renavam, company_id) FROM stdin;
dec0b3c8-4302-4aa7-a78b-3281e6337602	t	2026-04-08 20:27:37.074362+00	Admin Master	2026-06-01 12:50:54.133992+00	admin@admin.com	24000	Carreta	3	CZC-3363	56541235125	7f564f96-d90f-42cc-beb2-e37cf63a324d
01484413-6789-40b1-8d95-af53b235fc8b	t	2026-03-15 23:13:12.143619+00	Admin Master	2026-06-01 12:52:03.803+00	admin@admin.com	20000	Carreta	3	DAH-5853	12354689109	7f564f96-d90f-42cc-beb2-e37cf63a324d
614b9420-d569-4f8f-a63d-e260752db17c	t	2026-06-01 12:49:37.675201+00	admin@admin.com	2026-06-04 00:10:43.431368+00	admin@admin.com	22000	Cavalo	2	DPE-1919	21321458568	7f564f96-d90f-42cc-beb2-e37cf63a324d
dc4f5f09-1d69-4838-96bf-9e81639676f2	t	2026-06-01 13:02:04.118019+00	admin@admin.com	2026-06-04 00:11:02.724005+00	admin@admin.com	550	Fiorino	2	CNH-1513	21321458545	7f564f96-d90f-42cc-beb2-e37cf63a324d
60380c2a-025b-426f-b922-32c82f5209f4	t	2026-04-08 19:48:33.714133+00	Admin Master	2026-06-04 00:11:28.370257+00	admin@admin.com	650	Fiorino	3	ERP-0D21	12345678911	7f564f96-d90f-42cc-beb2-e37cf63a324d
bcf58715-e5ea-4e31-b356-d9b1abf6631c	t	2026-06-01 12:47:56.673897+00	admin@admin.com	2026-06-04 00:11:44.681981+00	admin@admin.com	24000	Cavalo	3	CNI-5243	21321458723	7f564f96-d90f-42cc-beb2-e37cf63a324d
35f4e73d-bb9e-43d3-b4c3-3220c5dac7e7	t	2026-06-04 12:21:15.060738+00	admin@admin.com	2026-06-04 12:33:38.748768+00	admin@admin.com	10000	Truck	2	CNI-2526	21321458542	7f564f96-d90f-42cc-beb2-e37cf63a324d
\.


--
-- TOC entry 3572 (class 0 OID 16561)
-- Dependencies: 227
-- Data for Name: group_transport_type_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.group_transport_type_table (equipament_group_id, type_transport_id) FROM stdin;
\.


--
-- TOC entry 3577 (class 0 OID 16762)
-- Dependencies: 232
-- Data for Name: occurrence_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.occurrence_table (id, active, created_at, created_by, updated_at, updated_by, attachment, description, type, delivery_id, sender_id, transport_id) FROM stdin;
\.


--
-- TOC entry 3575 (class 0 OID 16622)
-- Dependencies: 230
-- Data for Name: shipment_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.shipment_table (id, active, created_at, created_by, updated_at, updated_by, shipping_sequence, schedulind, status, volume, weight, shipment_customer_id, shipment_address_id, transport_id, shipment_type_id, type_transport_id, responsible_id, type_operation, carga_origem_id, route_planned, route_completed, calculated_distance, calculated_duration, calculated_wait, calculated_cost) FROM stdin;
2374516a-004d-47d4-ac14-ee490eba1aed	t	2026-06-01 15:59:48.88954+00	admin@admin.com	2026-06-04 17:04:30.479704+00	admin@admin.com	4	2026-06-11 18:00:00	AGUARDANDO INICIO	500	200	ab393f44-c83b-485e-b152-d6403e41163e	1f2f0764-3e1c-4665-aefa-daa1da658564	\N	877e764a-85aa-4857-b667-819480449650	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29	ENTREGA	8f720667-5f90-4fb6-919c-a7ee74afe792	v~qgCvua`HqA|CyAr@eNyHmGbR_OdIrQda@vLkDlAAr@v@wBra@M~S{@dBCbA	\N	12330	325	0	3.4175800000000005
1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	t	2026-05-03 16:01:35.684967+00	admin@admin.com	2026-06-05 01:51:36.911348+00	admin@admin.com	2	2026-06-11 11:00:00	AGUARDANDO INICIO	1200	18000	d9d7b435-c256-405b-877c-848f4a22e22a	550e8400-e29b-41d4-a716-446655440000	25d87ea6-ff19-490e-ae3f-37be5588c480	877e764a-85aa-4857-b667-819480449650	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29	COLETA	\N	~oqgCfke`H`@f@t@CZ]A_As@c@kCv@uNq@wD_AWb@PhAoGbQsPq@oOuCtDuO_FaBaAx@	\N	2122	313	8687	2.2705400000000004
1d1ef086-8077-4570-a4eb-6241fb27f08c	t	2026-05-21 22:31:45.45444+00	admin@admin.com	2026-06-05 01:51:36.912555+00	admin@admin.com	2	2026-06-11 13:00:00	AGUARDANDO INICIO	500	8000	eb97a8b5-a5ae-4985-a4d7-dbf30eefe347	1e58d64a-16bc-421d-b671-e0a52cb440a4	34ad53e0-d454-4524-aba5-ea1b369e5b5b	877e764a-85aa-4857-b667-819480449650	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29	ENTREGA	1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	|iogCzce`HeXhU{BtAwADqAtI}t@eDgBy@Da@}G@_Dr@mHEH_G	\N	2509	457	4943	3.53769
8f720667-5f90-4fb6-919c-a7ee74afe792	t	2026-05-03 16:03:31.795504+00	admin@admin.com	2026-06-05 01:51:36.911617+00	admin@admin.com	1	2026-06-11 08:00:00	AGUARDANDO INICIO	500	200	af29a1ab-407a-497d-b721-f3b93450d9eb	ace29b92-0e59-437a-b7f6-de8c2fa772c5	25d87ea6-ff19-490e-ae3f-37be5588c480	877e764a-85aa-4857-b667-819480449650	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29	COLETA	\N	v~qgCvua`HqA|CyAr@eNyHmGbR_OdIrQda@vLkDlAAr@v@wBra@M~S{@dBCbA	\N	3194	325	0	3.4175800000000005
a19c183d-a139-40b7-83c1-3c4014b4e5fa	t	2026-05-03 16:04:13.500902+00	admin@admin.com	2026-06-05 01:51:36.911859+00	admin@admin.com	3	2026-06-11 14:00:00	AGUARDANDO INICIO	200	6000	ab393f44-c83b-485e-b152-d6403e41163e	1f2f0764-3e1c-4665-aefa-daa1da658564	25d87ea6-ff19-490e-ae3f-37be5588c480	877e764a-85aa-4857-b667-819480449650	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29	ENTREGA	1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	|iogCzce`H}F~E_GgIqEb@mCe@uDaDHgFm@kCoAuAiDo@jDeK}@eDgBmOmAmLF{Aq@wAkCyVFyCaJut@c@iHsDsPXu@aDkF_PaOCw@g@C{EeWsIkWkFcMhIoEhEwDUYMwGVS]EOwG@{@\\LL_@o@a@SyIoGwPi@T	\N	7014	825	8175	7.504980000000001
57711563-d823-4374-bce1-63804c9ee425	t	2026-06-01 15:03:32.040696+00	admin@admin.com	2026-06-05 01:51:36.912373+00	admin@admin.com	2	2026-06-11 16:00:00	AGUARDANDO INICIO	500	4000	a92a50c7-b904-4637-b7ca-0f794ad50eaa	6e51b327-bec5-48eb-b881-76daf766ea1e	f5993e76-e2a9-4c7e-a899-2b269a12af67	877e764a-85aa-4857-b667-819480449650	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29	ENTREGA	1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	|iogCzce`H}F~E_GgImCb@qBGmBuFfKeEbA@Lu@xAy@l@eA~GeF|Aa@Wk@hBcL~A_Xb@qAOmDr@_Ol@oA[o@`AqS	\N	2867	378	15822	3.49774
\.


--
-- TOC entry 3576 (class 0 OID 16685)
-- Dependencies: 231
-- Data for Name: shipment_type; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.shipment_type (id, active, created_at, created_by, updated_at, updated_by, care, description, name) FROM stdin;
14123a31-3e20-48c7-b741-18f9e35a410e	f	2026-05-01 15:36:44.566682+00	admin@admin.com	2026-05-01 16:17:55.106352+00	admin@admin.com	Manter o veículo na temperatura especificada e verificar os lacres de segurança ao carregar.	Transporte de produtos perecíveis e medicamentos que exigem controle rigoroso de temperatura entre 5°C e 10°C.	Entrega Seca
ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	t	2026-05-01 15:43:52.785258+00	admin@admin.com	2026-05-01 16:21:26.256613+00	admin@admin.com	Manter o veículo na temperatura especificada e verificar os lacres de segurança ao carregar.	Transporte de produtos perecíveis e medicamentos que exigem controle rigoroso de temperatura entre 2°C e 8°C.	Entrega Refrigerada
877e764a-85aa-4857-b667-819480449650	t	2026-06-01 14:07:07.989264+00	admin@admin.com	2026-06-01 14:07:07.989264+00	admin@admin.com	Exige baú ou sider totalmente limpo, seco e vedado contra umidade ou infiltrações de água, além de empilhamento máximo conforme orientação das caixas para evitar o esmagamento das embalagens.	Transporte de produtos alimentícios industrializados de mercearia seca, não perecíveis e estáveis em temperatura ambiente.	ALIMENTOS MATINAIS
\.


--
-- TOC entry 3578 (class 0 OID 16790)
-- Dependencies: 233
-- Data for Name: telemetry_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.telemetry_table (id, active, created_at, created_by, updated_at, updated_by, alert, data1, data2, date_time, device, latitude, longitude, speed, equipament_id) FROM stdin;
03933bf7-cf6d-461f-9cfd-81567dafb6b8	t	2026-05-21 22:20:04.423653+00	admin@admin.com	2026-06-01 16:21:57.037758+00	admin@admin.com		teste		2026-06-11 20:15:20	Android	-22.37568666092154	-47.370114305457626	0	60380c2a-025b-426f-b922-32c82f5209f4
b3e1d4ba-3dbb-4dc3-a8e0-263e7f3cc548	t	2026-05-21 22:20:19.279312+00	admin@admin.com	2026-06-01 16:48:10.44907+00	admin@admin.com		teste		2026-06-10 20:15:20	Android	-22.347823828118702	-47.39677362320752	0	bcf58715-e5ea-4e31-b356-d9b1abf6631c
9dd76f29-b66a-4a62-9b9f-18e59ba8d208	t	2026-06-01 16:49:56.269676+00	admin@admin.com	2026-06-01 16:49:56.269676+00	admin@admin.com		teste		2026-06-10 19:15:20	Android	-22.359138884924523	-47.391468003949825	50	614b9420-d569-4f8f-a63d-e260752db17c
6894d38c-0b5b-4c84-a0d1-7b57f5c9d2c0	t	2026-06-01 16:53:44.226323+00	admin@admin.com	2026-06-01 16:53:44.226323+00	admin@admin.com		teste		2026-06-10 19:15:20	Android	-22.37970908477453	-47.362761604683065	0	dc4f5f09-1d69-4838-96bf-9e81639676f2
\.


--
-- TOC entry 3569 (class 0 OID 16431)
-- Dependencies: 224
-- Data for Name: tractor_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.tractor_table (type_fuel, id, km_per_liter, co2_per_km) FROM stdin;
DIESEL	614b9420-d569-4f8f-a63d-e260752db17c	1.9	1.41
GASOLINA	dc4f5f09-1d69-4838-96bf-9e81639676f2	13.6	0.17
GASOLINA	60380c2a-025b-426f-b922-32c82f5209f4	12.4	0.18
DIESEL	bcf58715-e5ea-4e31-b356-d9b1abf6631c	2.2	1.22
DIESEL	35f4e73d-bb9e-43d3-b4c3-3220c5dac7e7	2.5	1.07
\.


--
-- TOC entry 3570 (class 0 OID 16437)
-- Dependencies: 225
-- Data for Name: trailer_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.trailer_table (maximum_volume, id) FROM stdin;
100	01484413-6789-40b1-8d95-af53b235fc8b
100	dec0b3c8-4302-4aa7-a78b-3281e6337602
\.


--
-- TOC entry 3574 (class 0 OID 16590)
-- Dependencies: 229
-- Data for Name: transport_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.transport_table (id, shipment_quantity, route_return_planned, route_return_completed, time_stopped, distance_traveled, total_time, driver_id, equipament_group_id, transporter_id, created_at, created_by, updated_at, updated_by, active, route_planned, route_completed, calculed_distance, time_stopped_calculed, total_time_calculed, total_cost_calculated, total_cost, total_cost_calculed, code_transport) FROM stdin;
f5993e76-e2a9-4c7e-a899-2b269a12af67	2	\N	\N	\N	\N	\N	\N	9df01abb-f14a-49ef-955d-2d87185f8a51	7f564f96-d90f-42cc-beb2-e37cf63a324d	2026-06-05 01:51:36.813322+00	admin@admin.com	2026-06-05 01:51:36.912692+00	admin@admin.com	t	|iogCzce`H}F~E_GgImCb@qBGmBuFfKeEbA@Lu@xAy@l@eA~GeF|Aa@Wk@hBcL~A_Xb@qAOmDr@_Ol@oA[o@`AqS	\N	6769	29322	927	\N	\N	30.49079	10
34ad53e0-d454-4524-aba5-ea1b369e5b5b	2	\N	\N	\N	\N	\N	\N	d681c272-2657-4800-a298-9310aafdceda	7f564f96-d90f-42cc-beb2-e37cf63a324d	2026-06-05 01:51:36.851136+00	admin@admin.com	2026-06-05 01:51:36.912777+00	admin@admin.com	t	|iogCzce`HeXhU{BtAwADqAtI}t@eDgBy@Da@}G@_Dr@mHEH_G	\N	6411	29243	1006	\N	\N	30.49079	11
25d87ea6-ff19-490e-ae3f-37be5588c480	4	\N	\N	\N	\N	\N	\N	e56df2ca-a445-4e1e-ab3b-d1ba5bb9192b	7f564f96-d90f-42cc-beb2-e37cf63a324d	2026-06-05 01:51:36.889157+00	admin@admin.com	2026-06-05 01:51:36.913767+00	admin@admin.com	t	|iogCzce`H}F~E_GgIqEb@mCe@uDaDHgFm@kCoAuAiDo@jDeK}@eDgBmOmAmLF{Aq@wAkCyVFyCaJut@c@iHsDsPXu@aDkF_PaOCw@g@C{EeWsIkWkFcMhIoEhEwDUYMwGVS]EOwG@{@\\LL_@o@a@SyIoGwPi@T	\N	12330	35762	1463	\N	\N	30.49079	12
\.


--
-- TOC entry 3573 (class 0 OID 16568)
-- Dependencies: 228
-- Data for Name: type_transport_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.type_transport_table (id, active, created_at, created_by, updated_at, updated_by, care, description, name) FROM stdin;
2d23232b-898c-43a1-9729-c073883e82eb	t	2026-04-12 21:35:12.890605+00	Admin Master	2026-04-12 21:47:09.271674+00	Admin Master	Verificar o funcionamento do termostato a cada 4 horas e manter as portas seladas.	Transporte de carga com controle rigoroso de temperatura entre 0°C e 8°C.	REFRIGERADO
fd402f1c-49a6-414b-bc37-d79d7f4895d1	t	2026-04-12 21:52:24.364042+00	Admin Master	2026-04-12 21:52:24.364042+00	Admin Master	Exige motorista com curso MOPP, kit de emergência completo e painéis de segurança com número da ONU visíveis.	Transporte de substâncias inflamáveis, corrosivas ou tóxicas que exigem sinalização específica.	CARGA PERIGOSA
d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	t	2026-06-01 13:53:03.085394+00	admin@admin.com	2026-06-01 13:53:03.085394+00	admin@admin.com	Exige baú ou sider totalmente seco e vedado contra luz solar direta e umidade para evitar a contaminação ou empedramento dos produtos	Transporte de alimentos industrializados ensacados e enlatados, não perecíveis, organizados em pallets.	ALIMENTOS MATINAIS
\.


--
-- TOC entry 3571 (class 0 OID 16443)
-- Dependencies: 226
-- Data for Name: user_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.user_table (id, active, created_at, created_by, updated_at, updated_by, cpf, email, name, password, user_profile, company_id) FROM stdin;
c916e36f-4846-41be-9b32-9e0ff8850a29	t	2026-03-15 22:23:57.149323+00	\N	2026-03-15 22:23:57.149323+00	\N	000.000.000-00	admin@admin.com	Admin Master	$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi	ADMIN	d9d7b435-c256-405b-877c-848f4a22e22a
438eca76-a0cf-480b-8c85-f129ec3e9216	t	2026-03-15 23:04:31.316347+00	Admin Master	2026-03-15 23:04:31.316347+00	Admin Master	49018326895	jonathan@admin.com	Jonathan Alves	$2a$10$dSXyVxyZSainREynEB9SD.Lu1VNUkpvAtYukS0sns6JbAS8W7Sa6e	DRIVER	d9d7b435-c256-405b-877c-848f4a22e22a
\.


--
-- TOC entry 3587 (class 0 OID 0)
-- Dependencies: 235
-- Name: transport_table_code_transport_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.transport_table_code_transport_seq', 12, true);


--
-- TOC entry 3355 (class 2606 OID 16393)
-- Name: address_table address_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.address_table
    ADD CONSTRAINT address_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3389 (class 2606 OID 16820)
-- Name: collect_table collect_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collect_table
    ADD CONSTRAINT collect_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3357 (class 2606 OID 16402)
-- Name: company_table company_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT company_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3381 (class 2606 OID 16630)
-- Name: shipment_table delivery_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT delivery_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3383 (class 2606 OID 16693)
-- Name: shipment_type delivery_type_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_type
    ADD CONSTRAINT delivery_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3361 (class 2606 OID 16411)
-- Name: driver_table driver_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT driver_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3365 (class 2606 OID 16421)
-- Name: equipament_group_table equipament_group_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT equipament_group_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3367 (class 2606 OID 16430)
-- Name: equipament_table equipament_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_table
    ADD CONSTRAINT equipament_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3375 (class 2606 OID 16567)
-- Name: group_transport_type_table group_transport_type_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT group_transport_type_table_pkey PRIMARY KEY (equipament_group_id, type_transport_id);


--
-- TOC entry 3385 (class 2606 OID 16774)
-- Name: occurrence_table occurrence_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT occurrence_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3387 (class 2606 OID 16803)
-- Name: telemetry_table telemetry_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT telemetry_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3369 (class 2606 OID 16436)
-- Name: tractor_table tractor_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT tractor_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3371 (class 2606 OID 16442)
-- Name: trailer_table trailer_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT trailer_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3379 (class 2606 OID 16606)
-- Name: transport_table transport_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT transport_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3377 (class 2606 OID 16579)
-- Name: type_transport_table type_transport_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.type_transport_table
    ADD CONSTRAINT type_transport_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3359 (class 2606 OID 16454)
-- Name: company_table ukd521k4bkmmpvykqinw8y2xll4; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT ukd521k4bkmmpvykqinw8y2xll4 UNIQUE (address_id);


--
-- TOC entry 3363 (class 2606 OID 16456)
-- Name: driver_table ukljh0t57m3aq70di1sr03mkamm; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT ukljh0t57m3aq70di1sr03mkamm UNIQUE (user_id);


--
-- TOC entry 3373 (class 2606 OID 16452)
-- Name: user_table user_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT user_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3399 (class 2606 OID 16580)
-- Name: group_transport_type_table fk1bfj2jwykvj40s14xpbit6oek; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT fk1bfj2jwykvj40s14xpbit6oek FOREIGN KEY (type_transport_id) REFERENCES public.type_transport_table(id);


--
-- TOC entry 3401 (class 2606 OID 16612)
-- Name: transport_table fk1olqh7bbaqv47l0b82b0tao46; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fk1olqh7bbaqv47l0b82b0tao46 FOREIGN KEY (equipament_group_id) REFERENCES public.equipament_group_table(id);


--
-- TOC entry 3415 (class 2606 OID 16828)
-- Name: collect_table fk1v4kxyoyvnulwlkj2r2319iin; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collect_table
    ADD CONSTRAINT fk1v4kxyoyvnulwlkj2r2319iin FOREIGN KEY (customer_collects_id) REFERENCES public.company_table(id);


--
-- TOC entry 3390 (class 2606 OID 16457)
-- Name: company_table fk3pinakiucrrpni74drxw40wry; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT fk3pinakiucrrpni74drxw40wry FOREIGN KEY (address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3404 (class 2606 OID 16646)
-- Name: shipment_table fk5fvpu5fxacdhivqsbg0hvtmi3; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fk5fvpu5fxacdhivqsbg0hvtmi3 FOREIGN KEY (shipment_customer_id) REFERENCES public.company_table(id);


--
-- TOC entry 3405 (class 2606 OID 16661)
-- Name: shipment_table fk5vtw3imisq95otkp6nu1ykaix; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fk5vtw3imisq95otkp6nu1ykaix FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3406 (class 2606 OID 16651)
-- Name: shipment_table fk6icvyo9gc1gbwcjgpsrpgj5kq; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fk6icvyo9gc1gbwcjgpsrpgj5kq FOREIGN KEY (shipment_address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3411 (class 2606 OID 16780)
-- Name: occurrence_table fk7q3cx7bp1dxpx2boxw9jv2ew9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fk7q3cx7bp1dxpx2boxw9jv2ew9 FOREIGN KEY (sender_id) REFERENCES public.user_table(id);


--
-- TOC entry 3395 (class 2606 OID 16858)
-- Name: equipament_table fk84lg3hccqka845mvuf1kdmsdp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_table
    ADD CONSTRAINT fk84lg3hccqka845mvuf1kdmsdp FOREIGN KEY (company_id) REFERENCES public.company_table(id);


--
-- TOC entry 3414 (class 2606 OID 16804)
-- Name: telemetry_table fk8agm95f2i43eipl5gct1cooys; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT fk8agm95f2i43eipl5gct1cooys FOREIGN KEY (equipament_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3398 (class 2606 OID 16492)
-- Name: user_table fk8fosf57y9aqnb15l1sp7v6hx9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT fk8fosf57y9aqnb15l1sp7v6hx9 FOREIGN KEY (company_id) REFERENCES public.company_table(id);


--
-- TOC entry 3416 (class 2606 OID 16823)
-- Name: collect_table fk98bdo785jud8dhdkq401vwlfi; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collect_table
    ADD CONSTRAINT fk98bdo785jud8dhdkq401vwlfi FOREIGN KEY (address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3407 (class 2606 OID 16694)
-- Name: shipment_table fke26bb6rkrg9qbwg7nwbo70e5b; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fke26bb6rkrg9qbwg7nwbo70e5b FOREIGN KEY (shipment_type_id) REFERENCES public.shipment_type(id);


--
-- TOC entry 3412 (class 2606 OID 16775)
-- Name: occurrence_table fkex4dtns2sima7083o14xwapne; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fkex4dtns2sima7083o14xwapne FOREIGN KEY (delivery_id) REFERENCES public.shipment_table(id);


--
-- TOC entry 3408 (class 2606 OID 16849)
-- Name: shipment_table fkgt551brhr2yun1k0e8jta9gok; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkgt551brhr2yun1k0e8jta9gok FOREIGN KEY (carga_origem_id) REFERENCES public.shipment_table(id);


--
-- TOC entry 3392 (class 2606 OID 16467)
-- Name: equipament_group_table fkh75qjn0g3oegn5ah11dkjswtp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkh75qjn0g3oegn5ah11dkjswtp FOREIGN KEY (equipament1_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3393 (class 2606 OID 16477)
-- Name: equipament_group_table fkjrnonengpn01rsb9hgvsfufwn; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkjrnonengpn01rsb9hgvsfufwn FOREIGN KEY (equipament3_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3413 (class 2606 OID 16785)
-- Name: occurrence_table fkk9by0f888xgeaf06iayekcc1t; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fkk9by0f888xgeaf06iayekcc1t FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3409 (class 2606 OID 16671)
-- Name: shipment_table fkl4jj6d6icge0etvw7nni9y5b6; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkl4jj6d6icge0etvw7nni9y5b6 FOREIGN KEY (type_transport_id) REFERENCES public.type_transport_table(id);


--
-- TOC entry 3394 (class 2606 OID 16472)
-- Name: equipament_group_table fkmnl5l7kynnf1te95f6qdmf7y5; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkmnl5l7kynnf1te95f6qdmf7y5 FOREIGN KEY (equipament2_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3402 (class 2606 OID 16617)
-- Name: transport_table fknec9ddum3gpddey2lbnm9fsea; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fknec9ddum3gpddey2lbnm9fsea FOREIGN KEY (transporter_id) REFERENCES public.company_table(id);


--
-- TOC entry 3410 (class 2606 OID 16676)
-- Name: shipment_table fkng1lxvco0vtbwmbbrwb3vaauc; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkng1lxvco0vtbwmbbrwb3vaauc FOREIGN KEY (responsible_id) REFERENCES public.user_table(id);


--
-- TOC entry 3403 (class 2606 OID 16607)
-- Name: transport_table fknxy4bqqoludqmoryh5wdefbmh; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fknxy4bqqoludqmoryh5wdefbmh FOREIGN KEY (driver_id) REFERENCES public.driver_table(id);


--
-- TOC entry 3400 (class 2606 OID 16585)
-- Name: group_transport_type_table fkp4vpw3inbalv3b3hw184r9i37; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT fkp4vpw3inbalv3b3hw184r9i37 FOREIGN KEY (equipament_group_id) REFERENCES public.equipament_group_table(id);


--
-- TOC entry 3391 (class 2606 OID 16462)
-- Name: driver_table fkq31c4b12e6xftex24003i1qqd; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT fkq31c4b12e6xftex24003i1qqd FOREIGN KEY (user_id) REFERENCES public.user_table(id);


--
-- TOC entry 3397 (class 2606 OID 16487)
-- Name: trailer_table fkte47awk3lga1d0faeec89bhye; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT fkte47awk3lga1d0faeec89bhye FOREIGN KEY (id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3396 (class 2606 OID 16482)
-- Name: tractor_table fktpyy3ovef9vtfaubofrwkr6w1; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT fktpyy3ovef9vtfaubofrwkr6w1 FOREIGN KEY (id) REFERENCES public.equipament_table(id);


-- Completed on 2026-06-05 02:11:53 UTC

--
-- PostgreSQL database dump complete
--

\unrestrict g833rnksJzfREzbMJ6qJyyZXiNniCO0STiHI3sDfQ0zPkoNoN9OZZ20GpiVMdNc

