--
-- PostgreSQL database dump
--

\restrict h2Ime1RSPfFXaYRJR5uAJI3JCTPpphW6dEJC8Bi2EefB6s4DuCMHylvbhQ6VzIP

-- Dumped from database version 18.0 (Debian 18.0-1.pgdg13+3)
-- Dumped by pg_dump version 18.1

-- Started on 2026-05-21 22:39:32 UTC

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
    street character varying(255)
);


ALTER TABLE public.address_table OWNER TO admin;

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
-- TOC entry 230 (class 1259 OID 16622)
-- Name: delivery_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.delivery_table (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    delivery_sequence character varying(255),
    route_completed text,
    route_planned text,
    scheduled_collection timestamp(6) without time zone,
    scheduled_delivery timestamp(6) without time zone,
    status character varying(255),
    volume double precision,
    weight double precision,
    customer_collects uuid,
    customer_delivery uuid,
    destination_address_id uuid,
    origin_address_id uuid,
    transport_id uuid,
    type_delivery_id uuid,
    type_transport uuid,
    responsible_id uuid
);


ALTER TABLE public.delivery_table OWNER TO admin;

--
-- TOC entry 231 (class 1259 OID 16685)
-- Name: delivery_type; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.delivery_type (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    care character varying(255),
    description character varying(255),
    name character varying(255)
);


ALTER TABLE public.delivery_type OWNER TO admin;

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
    renavam character varying(255)
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
    id uuid NOT NULL
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
    delivery_quantity integer NOT NULL,
    route_return_planned character varying(255) NOT NULL,
    route_return_completed character varying(255) NOT NULL,
    time_stopped double precision NOT NULL,
    total_kilometer integer NOT NULL,
    total_time double precision NOT NULL,
    driver_id uuid NOT NULL,
    equipament_group_id uuid NOT NULL,
    transporter_id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    active boolean DEFAULT true
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
-- TOC entry 3552 (class 0 OID 16385)
-- Dependencies: 219
-- Data for Name: address_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.address_table (id, active, created_at, created_by, updated_at, updated_by, cep, city, complement, country, district, number, state, street) FROM stdin;
550e8400-e29b-41d4-a716-446655440000	t	\N	\N	\N	\N	13606062	Araras	Perto da Avenida da Saudade	Brasil	Jardim Industrial	437	SP	Avenida da Saudade
6e51b327-bec5-48eb-b881-76daf766ea1e	t	2026-05-03 15:38:33.274998+00	admin@admin.com	2026-05-03 15:38:33.274998+00	admin@admin.com	01234010	São Paulo	Museu do Futbol	Brasil	Pacaembu	147	SP	Praça Charles Miller
\.


--
-- TOC entry 3553 (class 0 OID 16394)
-- Dependencies: 220
-- Data for Name: company_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.company_table (id, active, created_at, created_by, updated_at, updated_by, cnpj_cpf, email, is_cliente, legal_name, phone_number, address_id) FROM stdin;
d9d7b435-c256-405b-877c-848f4a22e22a	t	2026-03-14 23:30:52.035627+00	\N	\N	\N	49018326895	golog@gmail.com	f	GoLog LTDA	(19)983282551	550e8400-e29b-41d4-a716-446655440000
a92a50c7-b904-4637-b7ca-0f794ad50eaa	t	2026-05-03 15:59:55.55528+00	admin@admin.com	2026-05-03 15:59:55.55528+00	admin@admin.com	12345678000190	contato@logtrans.com.br	t	LogTrans Transportes e Logística Ltda	(19)983282552	6e51b327-bec5-48eb-b881-76daf766ea1e
\.


--
-- TOC entry 3563 (class 0 OID 16622)
-- Dependencies: 230
-- Data for Name: delivery_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.delivery_table (id, active, created_at, created_by, updated_at, updated_by, delivery_sequence, route_completed, route_planned, scheduled_collection, scheduled_delivery, status, volume, weight, customer_collects, customer_delivery, destination_address_id, origin_address_id, transport_id, type_delivery_id, type_transport, responsible_id) FROM stdin;
1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	t	2026-05-03 16:01:35.684967+00	admin@admin.com	2026-05-03 16:01:35.684967+00	admin@admin.com	3	\N	[{"step":1,"location":"Centro de Distribuição Principal","timestamp":"2026-05-04T08:15:00Z"}]	2026-05-04 08:00:00	2026-05-05 16:30:00	EM TRANSITO	3.2	145.5	d9d7b435-c256-405b-877c-848f4a22e22a	a92a50c7-b904-4637-b7ca-0f794ad50eaa	6e51b327-bec5-48eb-b881-76daf766ea1e	550e8400-e29b-41d4-a716-446655440000	f8f40685-ddd3-4193-b959-40c88816f22b	ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	2d23232b-898c-43a1-9729-c073883e82eb	c916e36f-4846-41be-9b32-9e0ff8850a29
8f720667-5f90-4fb6-919c-a7ee74afe792	t	2026-05-03 16:03:31.795504+00	admin@admin.com	2026-05-03 16:03:31.795504+00	admin@admin.com	3	\N	[{"step":1,"location":"Centro de Distribuição Principal","timestamp":"2026-05-04T08:15:00Z"}]	2026-05-04 08:00:00	2026-05-05 16:30:00	EM TRANSITO	3.2	145.5	d9d7b435-c256-405b-877c-848f4a22e22a	a92a50c7-b904-4637-b7ca-0f794ad50eaa	6e51b327-bec5-48eb-b881-76daf766ea1e	550e8400-e29b-41d4-a716-446655440000	f8f40685-ddd3-4193-b959-40c88816f22b	ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	2d23232b-898c-43a1-9729-c073883e82eb	c916e36f-4846-41be-9b32-9e0ff8850a29
a19c183d-a139-40b7-83c1-3c4014b4e5fa	t	2026-05-03 16:04:13.500902+00	admin@admin.com	2026-05-03 16:04:13.500902+00	admin@admin.com	3	\N	[{"step":1,"location":"Centro de Distribuição Principal","timestamp":"2026-05-04T08:15:00Z"}]	2026-05-04 08:00:00	2026-05-05 16:30:00	EM TRANSITO	3.2	145.5	d9d7b435-c256-405b-877c-848f4a22e22a	a92a50c7-b904-4637-b7ca-0f794ad50eaa	6e51b327-bec5-48eb-b881-76daf766ea1e	550e8400-e29b-41d4-a716-446655440000	f8f40685-ddd3-4193-b959-40c88816f22b	ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	2d23232b-898c-43a1-9729-c073883e82eb	c916e36f-4846-41be-9b32-9e0ff8850a29
4907c218-f90e-4a0b-8e6b-af55da635d2f	f	2026-05-03 16:05:34.330227+00	admin@admin.com	2026-05-03 18:11:42.699388+00	admin@admin.com	3	[{"step":1,"location":"Centro de Distribuição Principal","timestamp":"2026-05-04T08:15:00Z"}]	[{"step":1,"location":"Centro de Distribuição Principal","coordinates":[-22.9068,-47.0616]},{"step": "location":"Ponto de Entrega A","coordinates":[-22.9123,-47.0541]}]	2026-05-04 08:00:00	2026-05-05 16:30:00	EM TRANSITO	2.2	200	d9d7b435-c256-405b-877c-848f4a22e22a	a92a50c7-b904-4637-b7ca-0f794ad50eaa	6e51b327-bec5-48eb-b881-76daf766ea1e	550e8400-e29b-41d4-a716-446655440000	f8f40685-ddd3-4193-b959-40c88816f22b	ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	2d23232b-898c-43a1-9729-c073883e82eb	c916e36f-4846-41be-9b32-9e0ff8850a29
1d1ef086-8077-4570-a4eb-6241fb27f08c	t	2026-05-21 22:31:45.45444+00	admin@admin.com	2026-05-21 22:31:45.45444+00	admin@admin.com	3	[{"step":1,"location":"Centro de Distribuição Principal","timestamp":"2026-05-04T08:15:00Z"},{"step":2,"location":"Ponto de Entrega A","timestamp":"2026-05-04T09:42:10Z"}]	[{"step":1,"location":"Centro de Distribuição Principal","coordinates":[-22.9068,-47.0616],"estimatedArrival":"2026-05-04T08:00:00Z"},{"step":2,"location":"Ponto de Entrega A","coordinates":[-22.9123,-47.0541],"estimatedArrival":"2026-05-04T09:30:00Z"},{"step":3,"location":"Ponto de Entrega B","coordinates":[-22.9205,-47.0422],"estimatedArrival":"2026-05-04T11:15:00Z"},{"step":4,"location":"Destino Final","coordinates":[-22.9312,-47.0285],"estimatedArrival":"2026-05-05T16:30:00Z"}]	2026-05-30 08:00:00	2026-06-05 16:30:00	EM TRANSITO	3.2	145.5	d9d7b435-c256-405b-877c-848f4a22e22a	a92a50c7-b904-4637-b7ca-0f794ad50eaa	6e51b327-bec5-48eb-b881-76daf766ea1e	550e8400-e29b-41d4-a716-446655440000	f8f40685-ddd3-4193-b959-40c88816f22b	ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	2d23232b-898c-43a1-9729-c073883e82eb	c916e36f-4846-41be-9b32-9e0ff8850a29
\.


--
-- TOC entry 3564 (class 0 OID 16685)
-- Dependencies: 231
-- Data for Name: delivery_type; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.delivery_type (id, active, created_at, created_by, updated_at, updated_by, care, description, name) FROM stdin;
14123a31-3e20-48c7-b741-18f9e35a410e	f	2026-05-01 15:36:44.566682+00	admin@admin.com	2026-05-01 16:17:55.106352+00	admin@admin.com	Manter o veículo na temperatura especificada e verificar os lacres de segurança ao carregar.	Transporte de produtos perecíveis e medicamentos que exigem controle rigoroso de temperatura entre 5°C e 10°C.	Entrega Seca
ab2bbdbc-823f-48a3-a48f-f6e9cb7de590	t	2026-05-01 15:43:52.785258+00	admin@admin.com	2026-05-01 16:21:26.256613+00	admin@admin.com	Manter o veículo na temperatura especificada e verificar os lacres de segurança ao carregar.	Transporte de produtos perecíveis e medicamentos que exigem controle rigoroso de temperatura entre 2°C e 8°C.	Entrega Refrigerada
\.


--
-- TOC entry 3554 (class 0 OID 16403)
-- Dependencies: 221
-- Data for Name: driver_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.driver_table (id, active, created_at, created_by, updated_at, updated_by, cnh_expiration, cnh_number, user_id) FROM stdin;
0fa0d515-0c4b-47be-bfd6-ef042669c0a0	t	2026-03-15 23:05:41.222869+00	Admin Master	2026-03-15 23:05:41.222869+00	Admin Master	2030-10-10	85983282551	438eca76-a0cf-480b-8c85-f129ec3e9216
\.


--
-- TOC entry 3555 (class 0 OID 16412)
-- Dependencies: 222
-- Data for Name: equipament_group_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_group_table (id, active, created_at, created_by, updated_at, updated_by, observation, equipament1_id, equipament2_id, equipament3_id) FROM stdin;
986a8c71-9ba4-47a4-b855-3fe37ca8ff9b	f	2026-04-08 22:37:46.137546+00	Admin Master	2026-04-08 22:37:46.137546+00	Admin Master	Conjunct 2	01484413-6789-40b1-8d95-af53b235fc8b	\N	\N
07796d8e-bacb-40f9-beb8-c02b6d00ff9d	t	2026-04-08 23:01:26.888952+00	Admin Master	2026-04-08 23:12:27.209113+00	Admin Master	Equipamento atualizado	60380c2a-025b-426f-b922-32c82f5209f4	\N	\N
d5820f33-c8d2-44a7-90b9-8914d127e77a	t	2026-04-08 23:14:57.914549+00	Admin Master	2026-04-08 23:14:57.914549+00	Admin Master	Conjunct 3	60380c2a-025b-426f-b922-32c82f5209f4	\N	\N
d681c272-2657-4800-a298-9310aafdceda	t	2026-04-08 23:15:26.413148+00	Admin Master	2026-04-08 23:15:26.413148+00	Admin Master	Conjunct 4	60380c2a-025b-426f-b922-32c82f5209f4	\N	\N
9df01abb-f14a-49ef-955d-2d87185f8a51	t	2026-04-08 23:21:24.827769+00	Admin Master	2026-04-08 23:21:24.827769+00	Admin Master	Conjunct 4	60380c2a-025b-426f-b922-32c82f5209f4	\N	\N
\.


--
-- TOC entry 3556 (class 0 OID 16422)
-- Dependencies: 223
-- Data for Name: equipament_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_table (id, active, created_at, created_by, updated_at, updated_by, maximum_capacity, model, number_axles, plate, renavam) FROM stdin;
60380c2a-025b-426f-b922-32c82f5209f4	t	2026-04-08 19:48:33.714133+00	Admin Master	2026-04-08 20:26:07.263481+00	Admin Master	20	Cavalo	3	ERP-0D21	12345678911
01484413-6789-40b1-8d95-af53b235fc8b	t	2026-03-15 23:13:12.143619+00	Admin Master	2026-04-08 20:39:12.044074+00	Admin Master	30	Carreta	4	CZC-3363	12354689109
dec0b3c8-4302-4aa7-a78b-3281e6337602	t	2026-04-08 20:27:37.074362+00	Admin Master	2026-04-08 21:12:34.883066+00	Admin Master	30	Carreta	3	CZC-3363	56541235125
\.


--
-- TOC entry 3560 (class 0 OID 16561)
-- Dependencies: 227
-- Data for Name: group_transport_type_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.group_transport_type_table (equipament_group_id, type_transport_id) FROM stdin;
9df01abb-f14a-49ef-955d-2d87185f8a51	2d23232b-898c-43a1-9729-c073883e82eb
\.


--
-- TOC entry 3565 (class 0 OID 16762)
-- Dependencies: 232
-- Data for Name: occurrence_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.occurrence_table (id, active, created_at, created_by, updated_at, updated_by, attachment, description, type, delivery_id, sender_id, transport_id) FROM stdin;
d1d523b2-787a-4e3b-9f6a-1aa8e4e35cad	f	2026-05-10 21:30:25.632698+00	admin@admin.com	2026-05-10 21:30:25.632698+00	admin@admin.com	https://storage.golog.com/evidencias/avaria_001.jpg	Identificada avaria na embalagem externa de 02 paletes durante o descarregamento no CD Araras.	AVARIA CARGA	1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	438eca76-a0cf-480b-8c85-f129ec3e9216	5a4629d6-9f62-4a90-9819-bc5a339899f0
ff7e545a-7bf0-4db6-88e5-c1bd33a1f18d	t	2026-05-10 21:23:03.191852+00	admin@admin.com	2026-05-10 22:05:50.85046+00	admin@admin.com	https://storage.golog.com/evidencias/avaria_001.jpg	Identificada avaria na embalagem externa de 02 paletes durante o descarregamento no CD Araras.	AVARIA_CARGA	1b152932-83f0-4d6e-8b7b-9cb17c0fce6e	438eca76-a0cf-480b-8c85-f129ec3e9216	5a4629d6-9f62-4a90-9819-bc5a339899f0
\.


--
-- TOC entry 3566 (class 0 OID 16790)
-- Dependencies: 233
-- Data for Name: telemetry_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.telemetry_table (id, active, created_at, created_by, updated_at, updated_by, alert, data1, data2, date_time, device, latitude, longitude, speed, equipament_id) FROM stdin;
03933bf7-cf6d-461f-9cfd-81567dafb6b8	t	2026-05-21 22:20:04.423653+00	admin@admin.com	2026-05-21 22:20:04.423653+00	admin@admin.com		teste		2026-10-02 19:17:20	Android	33.3604831	52.3798304	50	dec0b3c8-4302-4aa7-a78b-3281e6337602
b3e1d4ba-3dbb-4dc3-a8e0-263e7f3cc548	t	2026-05-21 22:20:19.279312+00	admin@admin.com	2026-05-21 22:20:19.279312+00	admin@admin.com		teste		2026-10-02 20:15:20	Android	36.3604831	62.3798304	70	dec0b3c8-4302-4aa7-a78b-3281e6337602
\.


--
-- TOC entry 3557 (class 0 OID 16431)
-- Dependencies: 224
-- Data for Name: tractor_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.tractor_table (type_fuel, id) FROM stdin;
Disel	60380c2a-025b-426f-b922-32c82f5209f4
\.


--
-- TOC entry 3558 (class 0 OID 16437)
-- Dependencies: 225
-- Data for Name: trailer_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.trailer_table (maximum_volume, id) FROM stdin;
100	01484413-6789-40b1-8d95-af53b235fc8b
100	dec0b3c8-4302-4aa7-a78b-3281e6337602
\.


--
-- TOC entry 3562 (class 0 OID 16590)
-- Dependencies: 229
-- Data for Name: transport_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.transport_table (id, delivery_quantity, route_return_planned, route_return_completed, time_stopped, total_kilometer, total_time, driver_id, equipament_group_id, transporter_id, created_at, created_by, updated_at, updated_by, active) FROM stdin;
5a4629d6-9f62-4a90-9819-bc5a339899f0	5	ROTA SUL - CD ARARAS PARA FILIAL PIRACICABA	ROTA SUL - VIA ANHANGUERA KM 120	0.75	180	4	0fa0d515-0c4b-47be-bfd6-ef042669c0a0	9df01abb-f14a-49ef-955d-2d87185f8a51	d9d7b435-c256-405b-877c-848f4a22e22a	\N	\N	2026-04-21 21:51:48.412192+00	admin@admin.com	t
f8f40685-ddd3-4193-b959-40c88816f22b	5	ROTA SUL - CD ARARAS PARA FILIAL CAMPINAS	ROTA SUL - VIA ANHANGUERA KM 120	0.75	180	4	0fa0d515-0c4b-47be-bfd6-ef042669c0a0	9df01abb-f14a-49ef-955d-2d87185f8a51	d9d7b435-c256-405b-877c-848f4a22e22a	2026-04-21 21:59:17.906277+00	admin@admin.com	2026-04-21 21:59:17.906277+00	admin@admin.com	t
\.


--
-- TOC entry 3561 (class 0 OID 16568)
-- Dependencies: 228
-- Data for Name: type_transport_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.type_transport_table (id, active, created_at, created_by, updated_at, updated_by, care, description, name) FROM stdin;
3cd5a5de-472b-4cf0-950c-5f7cc145d44e	f	2026-04-12 21:29:52.542063+00	Admin Master	2026-04-12 21:29:52.542063+00	Admin Master	Verificar o funcionamento do termostato a cada 4 horas e manter as portas seladas.	Transporte de carga com controle rigoroso de temperatura entre 0°C e 8°C.	REFRIGERADO
2d23232b-898c-43a1-9729-c073883e82eb	t	2026-04-12 21:35:12.890605+00	Admin Master	2026-04-12 21:47:09.271674+00	Admin Master	Verificar o funcionamento do termostato a cada 4 horas e manter as portas seladas.	Transporte de carga com controle rigoroso de temperatura entre 0°C e 8°C.	REFRIGERADO
fd402f1c-49a6-414b-bc37-d79d7f4895d1	t	2026-04-12 21:52:24.364042+00	Admin Master	2026-04-12 21:52:24.364042+00	Admin Master	Exige motorista com curso MOPP, kit de emergência completo e painéis de segurança com número da ONU visíveis.	Transporte de substâncias inflamáveis, corrosivas ou tóxicas que exigem sinalização específica.	CARGA PERIGOSA
\.


--
-- TOC entry 3559 (class 0 OID 16443)
-- Dependencies: 226
-- Data for Name: user_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.user_table (id, active, created_at, created_by, updated_at, updated_by, cpf, email, name, password, user_profile, company_id) FROM stdin;
c916e36f-4846-41be-9b32-9e0ff8850a29	t	2026-03-15 22:23:57.149323+00	\N	2026-03-15 22:23:57.149323+00	\N	000.000.000-00	admin@admin.com	Admin Master	$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi	ADMIN	d9d7b435-c256-405b-877c-848f4a22e22a
438eca76-a0cf-480b-8c85-f129ec3e9216	t	2026-03-15 23:04:31.316347+00	Admin Master	2026-03-15 23:04:31.316347+00	Admin Master	49018326895	jonathan@admin.com	Jonathan Alves	$2a$10$dSXyVxyZSainREynEB9SD.Lu1VNUkpvAtYukS0sns6JbAS8W7Sa6e	DRIVER	d9d7b435-c256-405b-877c-848f4a22e22a
\.


--
-- TOC entry 3347 (class 2606 OID 16393)
-- Name: address_table address_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.address_table
    ADD CONSTRAINT address_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3349 (class 2606 OID 16402)
-- Name: company_table company_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT company_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3373 (class 2606 OID 16630)
-- Name: delivery_table delivery_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT delivery_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3375 (class 2606 OID 16693)
-- Name: delivery_type delivery_type_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_type
    ADD CONSTRAINT delivery_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3353 (class 2606 OID 16411)
-- Name: driver_table driver_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT driver_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3357 (class 2606 OID 16421)
-- Name: equipament_group_table equipament_group_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT equipament_group_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3359 (class 2606 OID 16430)
-- Name: equipament_table equipament_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_table
    ADD CONSTRAINT equipament_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3367 (class 2606 OID 16567)
-- Name: group_transport_type_table group_transport_type_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT group_transport_type_table_pkey PRIMARY KEY (equipament_group_id, type_transport_id);


--
-- TOC entry 3377 (class 2606 OID 16774)
-- Name: occurrence_table occurrence_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT occurrence_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3379 (class 2606 OID 16803)
-- Name: telemetry_table telemetry_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT telemetry_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3361 (class 2606 OID 16436)
-- Name: tractor_table tractor_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT tractor_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3363 (class 2606 OID 16442)
-- Name: trailer_table trailer_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT trailer_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3371 (class 2606 OID 16606)
-- Name: transport_table transport_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT transport_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3369 (class 2606 OID 16579)
-- Name: type_transport_table type_transport_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.type_transport_table
    ADD CONSTRAINT type_transport_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3351 (class 2606 OID 16454)
-- Name: company_table ukd521k4bkmmpvykqinw8y2xll4; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT ukd521k4bkmmpvykqinw8y2xll4 UNIQUE (address_id);


--
-- TOC entry 3355 (class 2606 OID 16456)
-- Name: driver_table ukljh0t57m3aq70di1sr03mkamm; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT ukljh0t57m3aq70di1sr03mkamm UNIQUE (user_id);


--
-- TOC entry 3365 (class 2606 OID 16452)
-- Name: user_table user_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT user_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3388 (class 2606 OID 16580)
-- Name: group_transport_type_table fk1bfj2jwykvj40s14xpbit6oek; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT fk1bfj2jwykvj40s14xpbit6oek FOREIGN KEY (type_transport_id) REFERENCES public.type_transport_table(id);


--
-- TOC entry 3390 (class 2606 OID 16612)
-- Name: transport_table fk1olqh7bbaqv47l0b82b0tao46; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fk1olqh7bbaqv47l0b82b0tao46 FOREIGN KEY (equipament_group_id) REFERENCES public.equipament_group_table(id);


--
-- TOC entry 3380 (class 2606 OID 16457)
-- Name: company_table fk3pinakiucrrpni74drxw40wry; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT fk3pinakiucrrpni74drxw40wry FOREIGN KEY (address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3393 (class 2606 OID 16646)
-- Name: delivery_table fk5fvpu5fxacdhivqsbg0hvtmi3; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fk5fvpu5fxacdhivqsbg0hvtmi3 FOREIGN KEY (customer_delivery) REFERENCES public.company_table(id);


--
-- TOC entry 3394 (class 2606 OID 16661)
-- Name: delivery_table fk5vtw3imisq95otkp6nu1ykaix; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fk5vtw3imisq95otkp6nu1ykaix FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3395 (class 2606 OID 16651)
-- Name: delivery_table fk6icvyo9gc1gbwcjgpsrpgj5kq; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fk6icvyo9gc1gbwcjgpsrpgj5kq FOREIGN KEY (destination_address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3401 (class 2606 OID 16780)
-- Name: occurrence_table fk7q3cx7bp1dxpx2boxw9jv2ew9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fk7q3cx7bp1dxpx2boxw9jv2ew9 FOREIGN KEY (sender_id) REFERENCES public.user_table(id);


--
-- TOC entry 3404 (class 2606 OID 16804)
-- Name: telemetry_table fk8agm95f2i43eipl5gct1cooys; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT fk8agm95f2i43eipl5gct1cooys FOREIGN KEY (equipament_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3387 (class 2606 OID 16492)
-- Name: user_table fk8fosf57y9aqnb15l1sp7v6hx9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT fk8fosf57y9aqnb15l1sp7v6hx9 FOREIGN KEY (company_id) REFERENCES public.company_table(id);


--
-- TOC entry 3396 (class 2606 OID 16656)
-- Name: delivery_table fkaw4l72edjb7hmihb56rwcqwdy; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fkaw4l72edjb7hmihb56rwcqwdy FOREIGN KEY (origin_address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3397 (class 2606 OID 16694)
-- Name: delivery_table fke26bb6rkrg9qbwg7nwbo70e5b; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fke26bb6rkrg9qbwg7nwbo70e5b FOREIGN KEY (type_delivery_id) REFERENCES public.delivery_type(id);


--
-- TOC entry 3402 (class 2606 OID 16775)
-- Name: occurrence_table fkex4dtns2sima7083o14xwapne; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fkex4dtns2sima7083o14xwapne FOREIGN KEY (delivery_id) REFERENCES public.delivery_table(id);


--
-- TOC entry 3398 (class 2606 OID 16641)
-- Name: delivery_table fkfpw7whb0qx94o8le8s27l1ipp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fkfpw7whb0qx94o8le8s27l1ipp FOREIGN KEY (customer_collects) REFERENCES public.company_table(id);


--
-- TOC entry 3382 (class 2606 OID 16467)
-- Name: equipament_group_table fkh75qjn0g3oegn5ah11dkjswtp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkh75qjn0g3oegn5ah11dkjswtp FOREIGN KEY (equipament1_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3383 (class 2606 OID 16477)
-- Name: equipament_group_table fkjrnonengpn01rsb9hgvsfufwn; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkjrnonengpn01rsb9hgvsfufwn FOREIGN KEY (equipament3_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3403 (class 2606 OID 16785)
-- Name: occurrence_table fkk9by0f888xgeaf06iayekcc1t; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.occurrence_table
    ADD CONSTRAINT fkk9by0f888xgeaf06iayekcc1t FOREIGN KEY (transport_id) REFERENCES public.transport_table(id);


--
-- TOC entry 3399 (class 2606 OID 16671)
-- Name: delivery_table fkl4jj6d6icge0etvw7nni9y5b6; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fkl4jj6d6icge0etvw7nni9y5b6 FOREIGN KEY (type_transport) REFERENCES public.type_transport_table(id);


--
-- TOC entry 3384 (class 2606 OID 16472)
-- Name: equipament_group_table fkmnl5l7kynnf1te95f6qdmf7y5; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkmnl5l7kynnf1te95f6qdmf7y5 FOREIGN KEY (equipament2_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3391 (class 2606 OID 16617)
-- Name: transport_table fknec9ddum3gpddey2lbnm9fsea; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fknec9ddum3gpddey2lbnm9fsea FOREIGN KEY (transporter_id) REFERENCES public.company_table(id);


--
-- TOC entry 3400 (class 2606 OID 16676)
-- Name: delivery_table fkng1lxvco0vtbwmbbrwb3vaauc; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.delivery_table
    ADD CONSTRAINT fkng1lxvco0vtbwmbbrwb3vaauc FOREIGN KEY (responsible_id) REFERENCES public.user_table(id);


--
-- TOC entry 3392 (class 2606 OID 16607)
-- Name: transport_table fknxy4bqqoludqmoryh5wdefbmh; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transport_table
    ADD CONSTRAINT fknxy4bqqoludqmoryh5wdefbmh FOREIGN KEY (driver_id) REFERENCES public.driver_table(id);


--
-- TOC entry 3389 (class 2606 OID 16585)
-- Name: group_transport_type_table fkp4vpw3inbalv3b3hw184r9i37; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.group_transport_type_table
    ADD CONSTRAINT fkp4vpw3inbalv3b3hw184r9i37 FOREIGN KEY (equipament_group_id) REFERENCES public.equipament_group_table(id);


--
-- TOC entry 3381 (class 2606 OID 16462)
-- Name: driver_table fkq31c4b12e6xftex24003i1qqd; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT fkq31c4b12e6xftex24003i1qqd FOREIGN KEY (user_id) REFERENCES public.user_table(id);


--
-- TOC entry 3386 (class 2606 OID 16487)
-- Name: trailer_table fkte47awk3lga1d0faeec89bhye; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT fkte47awk3lga1d0faeec89bhye FOREIGN KEY (id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3385 (class 2606 OID 16482)
-- Name: tractor_table fktpyy3ovef9vtfaubofrwkr6w1; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT fktpyy3ovef9vtfaubofrwkr6w1 FOREIGN KEY (id) REFERENCES public.equipament_table(id);


-- Completed on 2026-05-21 22:39:32 UTC

--
-- PostgreSQL database dump complete
--

\unrestrict h2Ime1RSPfFXaYRJR5uAJI3JCTPpphW6dEJC8Bi2EefB6s4DuCMHylvbhQ6VzIP

