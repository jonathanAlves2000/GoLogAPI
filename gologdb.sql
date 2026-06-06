--
-- PostgreSQL database dump
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Dumped from database version 18.0 (Debian 18.0-1.pgdg13+3)
-- Dumped by pg_dump version 18.1

-- Started on 2026-06-06 23:46:07 UTC

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
-- TOC entry 231 (class 1259 OID 16809)
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
-- TOC entry 232 (class 1259 OID 16933)
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
    sender_id uuid NOT NULL,
    delivery_id uuid NOT NULL,
    transport_id uuid NOT NULL
);


ALTER TABLE public.occurrence_table OWNER TO admin;

--
-- TOC entry 235 (class 1259 OID 17112)
-- Name: route_stop_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.route_stop_table (
    id uuid NOT NULL,
    calculated_cost double precision,
    calculated_distance integer,
    calculated_duration integer,
    calculated_wait integer,
    realized_cots double precision,
    realized_distance integer,
    realized_duration integer,
    realized_wait integer,
    route_completed text,
    route_planned text,
    sequence_order integer NOT NULL,
    shipment_id uuid NOT NULL,
    transport_id uuid NOT NULL
);


ALTER TABLE public.route_stop_table OWNER TO admin;

--
-- TOC entry 233 (class 1259 OID 16946)
-- Name: shipment_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.shipment_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    schedulind timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    type_operation character varying(255),
    volume double precision NOT NULL,
    weight double precision NOT NULL,
    shipment_address_id uuid NOT NULL,
    shipment_customer_id uuid NOT NULL,
    carga_origem_id uuid,
    shipment_type_id uuid NOT NULL,
    transport_id uuid,
    type_transport_id uuid NOT NULL,
    responsible_id uuid NOT NULL,
    CONSTRAINT shipment_table_type_operation_check CHECK (((type_operation)::text = ANY ((ARRAY['ENTREGA'::character varying, 'COLETA'::character varying])::text[])))
);


ALTER TABLE public.shipment_table OWNER TO admin;

--
-- TOC entry 229 (class 1259 OID 16685)
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
-- TOC entry 230 (class 1259 OID 16790)
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
-- TOC entry 234 (class 1259 OID 16965)
-- Name: transport_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.transport_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    calculed_distance integer NOT NULL,
    code_transport integer,
    distance_traveled integer,
    route_completed text,
    route_planned text NOT NULL,
    route_return_completed character varying(255),
    route_return_planned character varying(255),
    shipment_quantity integer NOT NULL,
    time_stopped integer,
    time_stopped_calculed integer NOT NULL,
    total_cost double precision,
    total_cost_calculed double precision NOT NULL,
    total_time integer,
    total_time_calculed integer,
    driver_id uuid,
    equipament_group_id uuid NOT NULL,
    transporter_id uuid NOT NULL
);


ALTER TABLE public.transport_table OWNER TO admin;

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
-- TOC entry 3569 (class 0 OID 16385)
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
cb277df2-e4fc-4d9d-8948-7eccc7a3d556	t	2026-06-06 15:48:01.974693+00	admin@admin.com	2026-06-06 15:48:01.974693+00	admin@admin.com	13846150	Araras	Mercado Spani	Brasil	Jardim Santa Efigenia	3	SP	Av Pref Milton Severino	-22.354658131284378	-47.370722610747144
ff6ead7c-ceb6-4ffd-bef9-9e3782bed510	t	2026-06-06 15:57:22.627025+00	admin@admin.com	2026-06-06 15:57:22.627025+00	admin@admin.com	13607205	Araras	Mercado Tonin	Brasil	ardim das Flores	651	SP	Av Horácio Krepischi	-22.358687876787442	-47.36796568142011
\.


--
-- TOC entry 3581 (class 0 OID 16809)
-- Dependencies: 231
-- Data for Name: collect_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.collect_table (id, active, created_at, created_by, updated_at, updated_by, sequence, address_id, customer_collects_id, status, collection_schedule, route_planned, route_completed) FROM stdin;
9b8be550-57f6-4938-a1f8-f61f6c88cb05	t	2026-05-24 17:13:16.994212+00	admin@admin.com	2026-05-31 17:26:04.438486+00	admin@admin.com	1	ace29b92-0e59-437a-b7f6-de8c2fa772c5	af29a1ab-407a-497d-b721-f3b93450d9eb	EM TRANSITO	2026-06-11 13:50:30+00	-22.377182749785067, -47.382418513380095	-22.377182749785067, -47.382418513380095
\.


--
-- TOC entry 3570 (class 0 OID 16394)
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
ca7dee83-4beb-4e06-ae2b-b45b509e2720	t	2026-06-06 15:51:55.292648+00	admin@admin.com	2026-06-06 15:51:55.292648+00	admin@admin.com	05868574004278	spani@gmail.com	t	Spani Atacadista	(19)35435296	cb277df2-e4fc-4d9d-8948-7eccc7a3d556
754fcc36-c7be-4b0d-9a96-a504675ebb7e	t	2026-06-06 15:58:28.357548+00	admin@admin.com	2026-06-06 15:58:28.357548+00	admin@admin.com	24896425002990	tonin@gmail.com	t	Tonin Atacadista	(19)35455298	ff6ead7c-ceb6-4ffd-bef9-9e3782bed510
\.


--
-- TOC entry 3571 (class 0 OID 16403)
-- Dependencies: 221
-- Data for Name: driver_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.driver_table (id, active, created_at, created_by, updated_at, updated_by, cnh_expiration, cnh_number, user_id) FROM stdin;
0fa0d515-0c4b-47be-bfd6-ef042669c0a0	t	2026-03-15 23:05:41.222869+00	Admin Master	2026-03-15 23:05:41.222869+00	Admin Master	2030-10-10	85983282551	438eca76-a0cf-480b-8c85-f129ec3e9216
\.


--
-- TOC entry 3572 (class 0 OID 16412)
-- Dependencies: 222
-- Data for Name: equipament_group_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_group_table (id, active, created_at, created_by, updated_at, updated_by, observation, equipament1_id, equipament2_id, equipament3_id) FROM stdin;
07796d8e-bacb-40f9-beb8-c02b6d00ff9d	t	2026-04-08 23:01:26.888952+00	Admin Master	2026-06-01 12:58:48.170686+00	admin@admin.com	Conjunto 1	60380c2a-025b-426f-b922-32c82f5209f4	\N	\N
9df01abb-f14a-49ef-955d-2d87185f8a51	t	2026-04-08 23:21:24.827769+00	Admin Master	2026-06-01 12:59:52.238563+00	admin@admin.com	Conjunto 2	bcf58715-e5ea-4e31-b356-d9b1abf6631c	01484413-6789-40b1-8d95-af53b235fc8b	\N
d681c272-2657-4800-a298-9310aafdceda	t	2026-04-08 23:15:26.413148+00	Admin Master	2026-06-01 13:00:56.568358+00	admin@admin.com	Conjunto 3	614b9420-d569-4f8f-a63d-e260752db17c	dec0b3c8-4302-4aa7-a78b-3281e6337602	\N
d5820f33-c8d2-44a7-90b9-8914d127e77a	t	2026-04-08 23:14:57.914549+00	Admin Master	2026-06-01 13:03:02.604983+00	admin@admin.com	Conjunto 4	dc4f5f09-1d69-4838-96bf-9e81639676f2	\N	\N
e56df2ca-a445-4e1e-ab3b-d1ba5bb9192b	t	2026-06-04 12:22:44.086062+00	admin@admin.com	2026-06-04 12:22:44.086062+00	admin@admin.com	Conjunct 5	35f4e73d-bb9e-43d3-b4c3-3220c5dac7e7	\N	\N
17f54b48-7a3a-4a45-b7df-827bff7d9b05	t	2026-06-06 16:02:07.243877+00	admin@admin.com	2026-06-06 16:02:07.243877+00	admin@admin.com	Conjunct 5	19593a85-e1b3-4b2f-b747-112a13f1d5d5	\N	\N
\.


--
-- TOC entry 3573 (class 0 OID 16422)
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
19593a85-e1b3-4b2f-b747-112a13f1d5d5	t	2026-06-06 16:01:23.947602+00	admin@admin.com	2026-06-06 16:01:23.947602+00	admin@admin.com	12500	Truck	2	APE-8032	21322459542	7f564f96-d90f-42cc-beb2-e37cf63a324d
\.


--
-- TOC entry 3577 (class 0 OID 16561)
-- Dependencies: 227
-- Data for Name: group_transport_type_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.group_transport_type_table (equipament_group_id, type_transport_id) FROM stdin;
\.


--
-- TOC entry 3582 (class 0 OID 16933)
-- Dependencies: 232
-- Data for Name: occurrence_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.occurrence_table (id, active, created_at, created_by, updated_at, updated_by, attachment, description, type, sender_id, delivery_id, transport_id) FROM stdin;
\.


--
-- TOC entry 3585 (class 0 OID 17112)
-- Dependencies: 235
-- Data for Name: route_stop_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.route_stop_table (id, calculated_cost, calculated_distance, calculated_duration, calculated_wait, realized_cots, realized_distance, realized_duration, realized_wait, route_completed, route_planned, sequence_order, shipment_id, transport_id) FROM stdin;
9878b7f4-fb20-41be-810e-84febdaed5c1	0.54298	3194	325	0	\N	\N	\N	\N	\N	v~qgCvua`HqA|CyAr@eNyHmGbR_OdIrQda@vLkDlAAr@v@wBra@M~S{@dBCbA	1	486b3d5d-0466-4e76-9c04-e139921f15b7	9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5
b01ae7f7-1a93-4f8e-8111-9d46e1b24e75	0.59806	3518	349	3251	\N	\N	\N	\N	\N	~oqgCfke`H`@f@t@CZ]A_A}@c@aDv@uMq@eDaAuEGwSmGi@u@o@Tq\\eJgE_@kDaBaFoEiDkGeF_e@i@A}@rA_OvHy@JgD{@	2	daa19285-39c2-40cb-85ac-d7de4229e655	9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5
8f1ef6a0-9ff7-432e-aee9-fb419861b431	0.49045	2885	427	773	\N	\N	\N	\N	\N	jcmgClcc`HeBM}@|CeFdFr@j@vHcCbAAvKhLpAjBbFzLrB|AjD^~@p@t@hCSzEfHfHJfGv@ACcBfMQlB|BfAP`J_IiDw@aAx@	3	64dd3adc-8f9c-4b9e-a651-580bcead99cc	9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5
07548fba-6c96-4405-a8fc-b2f446460b00	0.42653	2509	457	19943	\N	\N	\N	\N	\N	|iogCzce`HeXhU{BtAwADqAtI}t@eDgBy@Da@}G@_Dr@mHEH_G	4	157fb504-8cf6-478f-9442-5410129c47ff	9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5
f3e73487-9874-4d8c-be1d-fd2ccee856a1	4.175140000000001	3902	549	0	\N	\N	\N	\N	\N	v~qgCvua`HqA|CyAr@eNyHeCbGwAnGoAnB}j@fZaBT{@hQaCdOaOzKHt@jDvAwHjDtBpFrG|HfAP`J_IiDw@aAx@	1	64dd3adc-8f9c-4b9e-a651-580bcead99cc	b56f4cc6-238e-4a76-9ee1-cefa260e43ec
dbe529ae-2c4e-4050-86e8-276991c2a743	3.0676900000000002	2867	378	5622	\N	\N	\N	\N	\N	|iogCzce`H}F~E_GgImCb@qBGmBuFfKeEbA@Lu@xAy@l@eA~GeF|Aa@Wk@hBcL~A_Xb@qAOmDr@_Ol@oA[o@`AqS	2	c826ec50-9680-42b7-a00a-64258f9ab8ad	b56f4cc6-238e-4a76-9ee1-cefa260e43ec
49b9c89f-0dc8-40fe-aa7c-15f8ea0b3085	3.06624	3194	325	0	\N	\N	\N	\N	\N	v~qgCvua`HqA|CyAr@eNyHmGbR_OdIrQda@vLkDlAAr@v@wBra@M~S{@dBCbA	1	486b3d5d-0466-4e76-9c04-e139921f15b7	13f08444-f4d4-4ed1-829a-235804528590
0e76e103-5b06-4dda-afee-50233966625e	2.03712	2122	313	6287	\N	\N	\N	\N	\N	~oqgCfke`H`@f@t@CZ]A_As@c@kCv@uNq@wD_AWb@PhAoGbQsPq@oOuCtDuO_FaBaAx@	2	64dd3adc-8f9c-4b9e-a651-580bcead99cc	13f08444-f4d4-4ed1-829a-235804528590
4bcd3587-8088-4d96-bdda-45c5ed8c830e	2.4604799999999996	2563	348	16452	\N	\N	\N	\N	\N	|iogCzce`H}F~E_GgIqEb@mCe@uDaDHgFm@kCoAuAgDk@hDiK}@eDwDw]l@u@jFu@tAFXi@g@]aGlAc@eAEcBsADKuA	3	614903d2-34be-418e-ae26-bfb17d327bc0	13f08444-f4d4-4ed1-829a-235804528590
c815a93a-c7b2-4989-9d68-12b1913d101e	5.071680000000001	5283	646	9554	\N	\N	\N	\N	\N	v|mgCxpb`HJtArAEDbBb@dA_BHg@cAkCyVFyCaJut@c@iHsDsPXu@aDkF_PaOCw@g@C{EeWsIkWkFcMhIoEhEwDUYMwGVS]EOwG@{@\\LL_@o@a@SyIoGwPi@T	4	a380d58f-d17b-4ef1-9058-17ba76a6c848	13f08444-f4d4-4ed1-829a-235804528590
\.


--
-- TOC entry 3583 (class 0 OID 16946)
-- Dependencies: 233
-- Data for Name: shipment_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.shipment_table (id, active, created_at, created_by, updated_at, updated_by, schedulind, status, type_operation, volume, weight, shipment_address_id, shipment_customer_id, carga_origem_id, shipment_type_id, transport_id, type_transport_id, responsible_id) FROM stdin;
64dd3adc-8f9c-4b9e-a651-580bcead99cc	t	2026-06-06 15:26:25.630314+00	admin@admin.com	2026-06-06 15:33:21.240954+00	admin@admin.com	2026-06-11 09:50:00		COLETA	800	13400	550e8400-e29b-41d4-a716-446655440000	d9d7b435-c256-405b-877c-848f4a22e22a	\N	877e764a-85aa-4857-b667-819480449650	\N	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
486b3d5d-0466-4e76-9c04-e139921f15b7	t	2026-06-06 15:25:41.607796+00	admin@admin.com	2026-06-06 15:59:29.351442+00	admin@admin.com	2026-06-11 07:30:00		COLETA	300	2850	ace29b92-0e59-437a-b7f6-de8c2fa772c5	af29a1ab-407a-497d-b721-f3b93450d9eb	\N	877e764a-85aa-4857-b667-819480449650	\N	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
c826ec50-9680-42b7-a00a-64258f9ab8ad	t	2026-06-06 15:28:33.901076+00	admin@admin.com	2026-06-06 16:05:34.368312+00	admin@admin.com	2026-06-11 12:00:00		ENTREGA	250	5000	6e51b327-bec5-48eb-b881-76daf766ea1e	a92a50c7-b904-4637-b7ca-0f794ad50eaa	64dd3adc-8f9c-4b9e-a651-580bcead99cc	877e764a-85aa-4857-b667-819480449650	b56f4cc6-238e-4a76-9ee1-cefa260e43ec	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
157fb504-8cf6-478f-9442-5410129c47ff	t	2026-06-06 15:33:21.231099+00	admin@admin.com	2026-06-06 16:05:34.368481+00	admin@admin.com	2026-06-11 16:00:00		ENTREGA	50	400	1e58d64a-16bc-421d-b671-e0a52cb440a4	eb97a8b5-a5ae-4985-a4d7-dbf30eefe347	64dd3adc-8f9c-4b9e-a651-580bcead99cc	877e764a-85aa-4857-b667-819480449650	9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
a380d58f-d17b-4ef1-9058-17ba76a6c848	t	2026-06-06 15:32:18.129253+00	admin@admin.com	2026-06-06 16:05:34.368564+00	admin@admin.com	2026-06-11 18:20:00		ENTREGA	500	8000	1f2f0764-3e1c-4665-aefa-daa1da658564	ab393f44-c83b-485e-b152-d6403e41163e	64dd3adc-8f9c-4b9e-a651-580bcead99cc	877e764a-85aa-4857-b667-819480449650	13f08444-f4d4-4ed1-829a-235804528590	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
daa19285-39c2-40cb-85ac-d7de4229e655	t	2026-06-06 15:54:06.754088+00	admin@admin.com	2026-06-06 16:05:34.368644+00	admin@admin.com	2026-06-11 09:00:00		ENTREGA	100	350	cb277df2-e4fc-4d9d-8948-7eccc7a3d556	ca7dee83-4beb-4e06-ae2b-b45b509e2720	486b3d5d-0466-4e76-9c04-e139921f15b7	877e764a-85aa-4857-b667-819480449650	9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
614903d2-34be-418e-ae26-bfb17d327bc0	t	2026-06-06 15:59:29.342995+00	admin@admin.com	2026-06-06 16:05:34.368723+00	admin@admin.com	2026-06-11 15:00:00		ENTREGA	200	2500	ff6ead7c-ceb6-4ffd-bef9-9e3782bed510	754fcc36-c7be-4b0d-9a96-a504675ebb7e	486b3d5d-0466-4e76-9c04-e139921f15b7	877e764a-85aa-4857-b667-819480449650	13f08444-f4d4-4ed1-829a-235804528590	d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	c916e36f-4846-41be-9b32-9e0ff8850a29
\.


--
-- TOC entry 3579 (class 0 OID 16685)
-- Dependencies: 229
-- Data for Name: shipment_type; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.shipment_type (id, active, created_at, created_by, updated_at, updated_by, care, description, name) FROM stdin;
14123a31-3e20-48c7-b741-18f9e35a410e	f	2026-05-01 15:36:44.566682+00	admin@admin.com	2026-05-01 16:17:55.106352+00	admin@admin.com	Manter o veículo na temperatura especificada e verificar os lacres de segurança ao carregar.	Transporte de produtos perecíveis e medicamentos que exigem controle rigoroso de temperatura entre 5°C e 10°C.	Entrega Seca
ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	t	2026-05-01 15:43:52.785258+00	admin@admin.com	2026-05-01 16:21:26.256613+00	admin@admin.com	Manter o veículo na temperatura especificada e verificar os lacres de segurança ao carregar.	Transporte de produtos perecíveis e medicamentos que exigem controle rigoroso de temperatura entre 2°C e 8°C.	Entrega Refrigerada
877e764a-85aa-4857-b667-819480449650	t	2026-06-01 14:07:07.989264+00	admin@admin.com	2026-06-01 14:07:07.989264+00	admin@admin.com	Exige baú ou sider totalmente limpo, seco e vedado contra umidade ou infiltrações de água, além de empilhamento máximo conforme orientação das caixas para evitar o esmagamento das embalagens.	Transporte de produtos alimentícios industrializados de mercearia seca, não perecíveis e estáveis em temperatura ambiente.	ALIMENTOS MATINAIS
70524754-52c2-41d6-94d1-a4c7307cd9c1	t	2026-06-06 15:12:00.414255+00	admin@admin.com	2026-06-06 15:12:00.414255+00	admin@admin.com	Exige alta acurácia no rastreamento em tempo real, baixa tolerância a atrasos, conferência rigorosa de canhoto/assinatura digital e cuidados com fragilidade para evitar avarias de pacotes individuais.	Distribuição B2C de mercadorias adquiridas via canais digitais, com foco em alta densidade de entregas urbanas e cumprimento de janelas comerciais restritas (prazos curtos).	Entrega E-commerce Padrão
\.


--
-- TOC entry 3580 (class 0 OID 16790)
-- Dependencies: 230
-- Data for Name: telemetry_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.telemetry_table (id, active, created_at, created_by, updated_at, updated_by, alert, data1, data2, date_time, device, latitude, longitude, speed, equipament_id) FROM stdin;
03933bf7-cf6d-461f-9cfd-81567dafb6b8	t	2026-05-21 22:20:04.423653+00	admin@admin.com	2026-06-01 16:21:57.037758+00	admin@admin.com		teste		2026-06-11 20:15:20	Android	-22.37568666092154	-47.370114305457626	0	60380c2a-025b-426f-b922-32c82f5209f4
b3e1d4ba-3dbb-4dc3-a8e0-263e7f3cc548	t	2026-05-21 22:20:19.279312+00	admin@admin.com	2026-06-01 16:48:10.44907+00	admin@admin.com		teste		2026-06-10 20:15:20	Android	-22.347823828118702	-47.39677362320752	0	bcf58715-e5ea-4e31-b356-d9b1abf6631c
9dd76f29-b66a-4a62-9b9f-18e59ba8d208	t	2026-06-01 16:49:56.269676+00	admin@admin.com	2026-06-01 16:49:56.269676+00	admin@admin.com		teste		2026-06-10 19:15:20	Android	-22.359138884924523	-47.391468003949825	50	614b9420-d569-4f8f-a63d-e260752db17c
6894d38c-0b5b-4c84-a0d1-7b57f5c9d2c0	t	2026-06-01 16:53:44.226323+00	admin@admin.com	2026-06-01 16:53:44.226323+00	admin@admin.com		teste		2026-06-10 19:15:20	Android	-22.37970908477453	-47.362761604683065	0	dc4f5f09-1d69-4838-96bf-9e81639676f2
\.


--
-- TOC entry 3574 (class 0 OID 16431)
-- Dependencies: 224
-- Data for Name: tractor_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.tractor_table (type_fuel, id, km_per_liter, co2_per_km) FROM stdin;
DIESEL	614b9420-d569-4f8f-a63d-e260752db17c	1.9	1.41
GASOLINA	dc4f5f09-1d69-4838-96bf-9e81639676f2	13.6	0.17
GASOLINA	60380c2a-025b-426f-b922-32c82f5209f4	12.4	0.18
DIESEL	bcf58715-e5ea-4e31-b356-d9b1abf6631c	2.2	1.22
DIESEL	35f4e73d-bb9e-43d3-b4c3-3220c5dac7e7	2.5	1.07
DIESEL	19593a85-e1b3-4b2f-b747-112a13f1d5d5	2.8	0.96
\.


--
-- TOC entry 3575 (class 0 OID 16437)
-- Dependencies: 225
-- Data for Name: trailer_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.trailer_table (maximum_volume, id) FROM stdin;
100	01484413-6789-40b1-8d95-af53b235fc8b
100	dec0b3c8-4302-4aa7-a78b-3281e6337602
\.


--
-- TOC entry 3584 (class 0 OID 16965)
-- Dependencies: 234
-- Data for Name: transport_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.transport_table (id, active, created_at, created_by, updated_at, updated_by, calculed_distance, code_transport, distance_traveled, route_completed, route_planned, route_return_completed, route_return_planned, shipment_quantity, time_stopped, time_stopped_calculed, total_cost, total_cost_calculed, total_time, total_time_calculed, driver_id, equipament_group_id, transporter_id) FROM stdin;
b56f4cc6-238e-4a76-9ee1-cefa260e43ec	t	2026-06-06 16:05:34.321575+00	admin@admin.com	2026-06-06 19:14:24.278483+00	admin@admin.com	6769	\N	\N	\N	v~qgCvua`HqA|CyAr@eNyHeCbGwAnGoAnB}j@fZaBT{@hQaCdOaOzKHt@jDvAwHjDtBpFrG|HfAP`J_IiDw@aAx@??}F~E_GgImCb@qBGmBuFfKeEbA@Lu@xAy@l@eA~GeF|Aa@Wk@hBcL~A_Xb@qAOmDr@_Ol@oA[o@`AqS	\N	\N	2	\N	33522	\N	7.2428300000000005	\N	927	\N	e56df2ca-a445-4e1e-ab3b-d1ba5bb9192b	7f564f96-d90f-42cc-beb2-e37cf63a324d
9b7f1cc3-11e5-45a2-bd2d-4c765faa8ae5	t	2026-06-06 16:05:34.284106+00	admin@admin.com	2026-06-06 19:14:24.278582+00	admin@admin.com	12106	\N	\N	\N	v~qgCvua`HqA|CyAr@eNyHmGbR_OdIrQda@vLkDlAAr@v@wBra@M~S{@dBCbA??`@f@t@CZ]A_A}@c@aDv@uMq@eDaAuEGwSmGi@u@o@Tq\\eJgE_@kDaBaFoEiDkGeF_e@i@A}@rA_OvHy@JgD{@??eBM}@|CeFdFr@j@vHcCbAAvKhLpAjBbFzLrB|AjD^~@p@t@hCSzEfHfHJfGv@ACcBfMQlB|BfAP`J_IiDw@aAx@??eXhU{BtAwADqAtI}t@eDgBy@Da@}G@_Dr@mHEH_G	\N	\N	4	\N	37467	\N	2.05802	\N	1558	\N	d5820f33-c8d2-44a7-90b9-8914d127e77a	7f564f96-d90f-42cc-beb2-e37cf63a324d
13f08444-f4d4-4ed1-829a-235804528590	t	2026-06-06 16:05:34.343506+00	admin@admin.com	2026-06-06 19:14:24.278635+00	admin@admin.com	13162	\N	\N	\N	v~qgCvua`HqA|CyAr@eNyHmGbR_OdIrQda@vLkDlAAr@v@wBra@M~S{@dBCbA??`@f@t@CZ]A_As@c@kCv@uNq@wD_AWb@PhAoGbQsPq@oOuCtDuO_FaBaAx@??}F~E_GgIqEb@mCe@uDaDHgFm@kCoAuAgDk@hDiK}@eDwDw]l@u@jFu@tAFXi@g@]aGlAc@eAEcBsADKuA??JtArAEDbBb@dA_BHg@cAkCyVFyCaJut@c@iHsDsPXu@aDkF_PaOCw@g@C{EeWsIkWkFcMhIoEhEwDUYMwGVS]EOwG@{@\\LL_@o@a@SyIoGwPi@T	\N	\N	4	\N	37393	\N	12.63552	\N	1632	\N	17f54b48-7a3a-4a45-b7df-827bff7d9b05	7f564f96-d90f-42cc-beb2-e37cf63a324d
\.


--
-- TOC entry 3578 (class 0 OID 16568)
-- Dependencies: 228
-- Data for Name: type_transport_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.type_transport_table (id, active, created_at, created_by, updated_at, updated_by, care, description, name) FROM stdin;
2d23232b-898c-43a1-9729-c073883e82eb	t	2026-04-12 21:35:12.890605+00	Admin Master	2026-04-12 21:47:09.271674+00	Admin Master	Verificar o funcionamento do termostato a cada 4 horas e manter as portas seladas.	Transporte de carga com controle rigoroso de temperatura entre 0°C e 8°C.	REFRIGERADO
fd402f1c-49a6-414b-bc37-d79d7f4895d1	t	2026-04-12 21:52:24.364042+00	Admin Master	2026-04-12 21:52:24.364042+00	Admin Master	Exige motorista com curso MOPP, kit de emergência completo e painéis de segurança com número da ONU visíveis.	Transporte de substâncias inflamáveis, corrosivas ou tóxicas que exigem sinalização específica.	CARGA PERIGOSA
d12c989b-46d3-4ebd-9ef4-d7f35a9a225f	t	2026-06-01 13:53:03.085394+00	admin@admin.com	2026-06-01 13:53:03.085394+00	admin@admin.com	Exige baú ou sider totalmente seco e vedado contra luz solar direta e umidade para evitar a contaminação ou empedramento dos produtos	Transporte de alimentos industrializados ensacados e enlatados, não perecíveis, organizados em pallets.	ALIMENTOS MATINAIS
\.


--
-- TOC entry 3576 (class 0 OID 16443)
-- Dependencies: 226
-- Data for Name: user_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.user_table (id, active, created_at, created_by, updated_at, updated_by, cpf, email, name, password, user_profile, company_id) FROM stdin;
c916e36f-4846-41be-9b32-9e0ff8850a29	t	2026-03-15 22:23:57.149323+00	\N	2026-03-15 22:23:57.149323+00	\N	000.000.000-00	admin@admin.com	Admin Master	$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi	ADMIN	d9d7b435-c256-405b-877c-848f4a22e22a
438eca76-a0cf-480b-8c85-f129ec3e9216	t	2026-03-15 23:04:31.316347+00	Admin Master	2026-03-15 23:04:31.316347+00	Admin Master	49018326895	jonathan@admin.com	Jonathan Alves	$2a$10$dSXyVxyZSainREynEB9SD.Lu1VNUkpvAtYukS0sns6JbAS8W7Sa6e	DRIVER	d9d7b435-c256-405b-877c-848f4a22e22a
\.


--
-- TOC entry 3356 (class 2606 OID 16393)
-- Name: address_table address_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.address_table
    ADD CONSTRAINT address_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3384 (class 2606 OID 16820)
-- Name: collect_table collect_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collect_table
    ADD CONSTRAINT collect_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3358 (class 2606 OID 16402)
-- Name: company_table company_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT company_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3380 (class 2606 OID 16693)
-- Name: shipment_type delivery_type_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_type
    ADD CONSTRAINT delivery_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3362 (class 2606 OID 16411)
-- Name: driver_table driver_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT driver_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3366 (class 2606 OID 16421)
-- Name: equipament_group_table equipament_group_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT equipament_group_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3368 (class 2606 OID 16430)
-- Name: equipament_table equipament_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_table
    ADD CONSTRAINT equipament_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3376 (class 2606 OID 16567)
-- Name: group_transport_type_table group_transport_type_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT group_transport_type_table_pkey PRIMARY KEY (equipament_group_id, type_transport_id);


--
-- TOC entry 3386 (class 2606 OID 16945)
-- Name: occurrence_table occurrence_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT occurrence_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3392 (class 2606 OID 17122)
-- Name: route_stop_table route_stop_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.route_stop_table
    ADD CONSTRAINT route_stop_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3388 (class 2606 OID 16964)
-- Name: shipment_table shipment_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT shipment_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3382 (class 2606 OID 16803)
-- Name: telemetry_table telemetry_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT telemetry_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3370 (class 2606 OID 16436)
-- Name: tractor_table tractor_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT tractor_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3372 (class 2606 OID 16442)
-- Name: trailer_table trailer_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT trailer_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3390 (class 2606 OID 16981)
-- Name: transport_table transport_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT transport_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3378 (class 2606 OID 16579)
-- Name: type_transport_table type_transport_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.type_transport_table
    ADD CONSTRAINT type_transport_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3360 (class 2606 OID 16454)
-- Name: company_table ukd521k4bkmmpvykqinw8y2xll4; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT ukd521k4bkmmpvykqinw8y2xll4 UNIQUE (address_id);


--
-- TOC entry 3364 (class 2606 OID 16456)
-- Name: driver_table ukljh0t57m3aq70di1sr03mkamm; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT ukljh0t57m3aq70di1sr03mkamm UNIQUE (user_id);


--
-- TOC entry 3374 (class 2606 OID 16452)
-- Name: user_table user_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT user_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3402 (class 2606 OID 16580)
-- Name: group_transport_type_table fk1bfj2jwykvj40s14xpbit6oek; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT fk1bfj2jwykvj40s14xpbit6oek FOREIGN KEY (type_transport_id) REFERENCES public.type_transport_table(id);


--
-- TOC entry 3417 (class 2606 OID 17037)
-- Name: transport_table fk1olqh7bbaqv47l0b82b0tao46; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fk1olqh7bbaqv47l0b82b0tao46 FOREIGN KEY (equipament_group_id) REFERENCES public.equipament_group_table(id);


--
-- TOC entry 3405 (class 2606 OID 16828)
-- Name: collect_table fk1v4kxyoyvnulwlkj2r2319iin; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collect_table
    ADD CONSTRAINT fk1v4kxyoyvnulwlkj2r2319iin FOREIGN KEY (customer_collects_id) REFERENCES public.company_table(id);


--
-- TOC entry 3420 (class 2606 OID 17123)
-- Name: route_stop_table fk3o9wsk9jdiatlr52na36cfgrr; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.route_stop_table
    ADD CONSTRAINT fk3o9wsk9jdiatlr52na36cfgrr FOREIGN KEY (shipment_id) REFERENCES public.shipment_table(id);


--
-- TOC entry 3393 (class 2606 OID 16457)
-- Name: company_table fk3pinakiucrrpni74drxw40wry; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT fk3pinakiucrrpni74drxw40wry FOREIGN KEY (address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3421 (class 2606 OID 17128)
-- Name: route_stop_table fk72w9he85badavpb6ad3ofjtp2; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.route_stop_table
    ADD CONSTRAINT fk72w9he85badavpb6ad3ofjtp2 FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3407 (class 2606 OID 16982)
-- Name: occurrence_table fk7q3cx7bp1dxpx2boxw9jv2ew9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fk7q3cx7bp1dxpx2boxw9jv2ew9 FOREIGN KEY (sender_id) REFERENCES public.user_table(id);


--
-- TOC entry 3398 (class 2606 OID 16858)
-- Name: equipament_table fk84lg3hccqka845mvuf1kdmsdp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_table
    ADD CONSTRAINT fk84lg3hccqka845mvuf1kdmsdp FOREIGN KEY (company_id) REFERENCES public.company_table(id);


--
-- TOC entry 3404 (class 2606 OID 16804)
-- Name: telemetry_table fk8agm95f2i43eipl5gct1cooys; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT fk8agm95f2i43eipl5gct1cooys FOREIGN KEY (equipament_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3401 (class 2606 OID 16492)
-- Name: user_table fk8fosf57y9aqnb15l1sp7v6hx9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT fk8fosf57y9aqnb15l1sp7v6hx9 FOREIGN KEY (company_id) REFERENCES public.company_table(id);


--
-- TOC entry 3406 (class 2606 OID 16823)
-- Name: collect_table fk98bdo785jud8dhdkq401vwlfi; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collect_table
    ADD CONSTRAINT fk98bdo785jud8dhdkq401vwlfi FOREIGN KEY (address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3410 (class 2606 OID 17017)
-- Name: shipment_table fkc9peqnuj0eqovlncfs5qfpka3; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkc9peqnuj0eqovlncfs5qfpka3 FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3411 (class 2606 OID 17002)
-- Name: shipment_table fkcgv9usnlrjtyy6jqi2swc8bt4; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkcgv9usnlrjtyy6jqi2swc8bt4 FOREIGN KEY (shipment_customer_id) REFERENCES public.company_table(id);


--
-- TOC entry 3412 (class 2606 OID 16997)
-- Name: shipment_table fkglufum5fu0woe8l5sgvv2caqy; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkglufum5fu0woe8l5sgvv2caqy FOREIGN KEY (shipment_address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3413 (class 2606 OID 17007)
-- Name: shipment_table fkgt551brhr2yun1k0e8jta9gok; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkgt551brhr2yun1k0e8jta9gok FOREIGN KEY (carga_origem_id) REFERENCES public.shipment_table(id);


--
-- TOC entry 3395 (class 2606 OID 16467)
-- Name: equipament_group_table fkh75qjn0g3oegn5ah11dkjswtp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkh75qjn0g3oegn5ah11dkjswtp FOREIGN KEY (equipament1_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3414 (class 2606 OID 17027)
-- Name: shipment_table fkj4lv0k7r84uy4a21y9b1osq9q; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkj4lv0k7r84uy4a21y9b1osq9q FOREIGN KEY (responsible_id) REFERENCES public.user_table(id);


--
-- TOC entry 3396 (class 2606 OID 16477)
-- Name: equipament_group_table fkjrnonengpn01rsb9hgvsfufwn; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkjrnonengpn01rsb9hgvsfufwn FOREIGN KEY (equipament3_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3408 (class 2606 OID 16992)
-- Name: occurrence_table fkk9by0f888xgeaf06iayekcc1t; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fkk9by0f888xgeaf06iayekcc1t FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3415 (class 2606 OID 17022)
-- Name: shipment_table fklwth0k8ilxqqgqm4w2b74ui38; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fklwth0k8ilxqqgqm4w2b74ui38 FOREIGN KEY (type_transport_id) REFERENCES public.type_transport_table(id);


--
-- TOC entry 3397 (class 2606 OID 16472)
-- Name: equipament_group_table fkmnl5l7kynnf1te95f6qdmf7y5; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkmnl5l7kynnf1te95f6qdmf7y5 FOREIGN KEY (equipament2_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3416 (class 2606 OID 17012)
-- Name: shipment_table fkmqb3pgh812dxho5rxniyv576i; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.shipment_table
    ADD CONSTRAINT fkmqb3pgh812dxho5rxniyv576i FOREIGN KEY (shipment_type_id) REFERENCES public.shipment_type(id);


--
-- TOC entry 3418 (class 2606 OID 17042)
-- Name: transport_table fknec9ddum3gpddey2lbnm9fsea; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fknec9ddum3gpddey2lbnm9fsea FOREIGN KEY (transporter_id) REFERENCES public.company_table(id);


--
-- TOC entry 3419 (class 2606 OID 17032)
-- Name: transport_table fknxy4bqqoludqmoryh5wdefbmh; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fknxy4bqqoludqmoryh5wdefbmh FOREIGN KEY (driver_id) REFERENCES public.driver_table(id);


--
-- TOC entry 3403 (class 2606 OID 16585)
-- Name: group_transport_type_table fkp4vpw3inbalv3b3hw184r9i37; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT fkp4vpw3inbalv3b3hw184r9i37 FOREIGN KEY (equipament_group_id) REFERENCES public.equipament_group_table(id);


--
-- TOC entry 3394 (class 2606 OID 16462)
-- Name: driver_table fkq31c4b12e6xftex24003i1qqd; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT fkq31c4b12e6xftex24003i1qqd FOREIGN KEY (user_id) REFERENCES public.user_table(id);


--
-- TOC entry 3409 (class 2606 OID 16987)
-- Name: occurrence_table fkq8hvp7bmro6xsr237qy6yha1q; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fkq8hvp7bmro6xsr237qy6yha1q FOREIGN KEY (delivery_id) REFERENCES public.shipment_table(id);


--
-- TOC entry 3400 (class 2606 OID 16487)
-- Name: trailer_table fkte47awk3lga1d0faeec89bhye; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT fkte47awk3lga1d0faeec89bhye FOREIGN KEY (id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3399 (class 2606 OID 16482)
-- Name: tractor_table fktpyy3ovef9vtfaubofrwkr6w1; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT fktpyy3ovef9vtfaubofrwkr6w1 FOREIGN KEY (id) REFERENCES public.equipament_table(id);


-- Completed on 2026-06-06 23:46:07 UTC

--
-- PostgreSQL database dump complete
--

\unrestrict XMfVDgvhxuC0XrOXReee22wSG5rLKs9iK0d42fjDOTzMNhiDjNqu85neAukdDRJ

