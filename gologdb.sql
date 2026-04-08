--
-- PostgreSQL database dump
--

\restrict sgXnLKRZDIjZqk1IKEG9bAe6SrTIsSMYvtTlEgNoBkYIyDz73kemg6rtJaBDaYl

-- Dumped from database version 18.0 (Debian 18.0-1.pgdg13+3)
-- Dumped by pg_dump version 18.1

-- Started on 2026-04-08 01:27:38 UTC

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
-- TOC entry 227 (class 1259 OID 16540)
-- Name: telemetry_table; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.telemetry_table (
    id uuid NOT NULL,
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
-- TOC entry 3499 (class 0 OID 16385)
-- Dependencies: 219
-- Data for Name: address_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.address_table (id, active, created_at, created_by, updated_at, updated_by, cep, city, complement, country, district, number, state, street) FROM stdin;
550e8400-e29b-41d4-a716-446655440000	t	\N	\N	\N	\N	13606062	Araras	Perto da Avenida da Saudade	Brasil	Jardim Industrial	437	SP	Avenida da Saudade
\.


--
-- TOC entry 3500 (class 0 OID 16394)
-- Dependencies: 220
-- Data for Name: company_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.company_table (id, active, created_at, created_by, updated_at, updated_by, cnpj_cpf, email, is_cliente, legal_name, phone_number, address_id) FROM stdin;
d9d7b435-c256-405b-877c-848f4a22e22a	t	2026-03-14 23:30:52.035627+00	\N	\N	\N	49018326895	golog@gmail.com	f	GoLog LTDA	(19)983282551	550e8400-e29b-41d4-a716-446655440000
\.


--
-- TOC entry 3501 (class 0 OID 16403)
-- Dependencies: 221
-- Data for Name: driver_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.driver_table (id, active, created_at, created_by, updated_at, updated_by, cnh_expiration, cnh_number, user_id) FROM stdin;
0fa0d515-0c4b-47be-bfd6-ef042669c0a0	t	2026-03-15 23:05:41.222869+00	Admin Master	2026-03-15 23:05:41.222869+00	Admin Master	2030-10-10	85983282551	438eca76-a0cf-480b-8c85-f129ec3e9216
\.


--
-- TOC entry 3502 (class 0 OID 16412)
-- Dependencies: 222
-- Data for Name: equipament_group_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_group_table (id, active, created_at, created_by, updated_at, updated_by, observation, equipament1_id, equipament2_id, equipament3_id) FROM stdin;
0b80d7bf-010f-4f3f-9bf0-062aa502eeab	t	2026-03-15 23:06:45.083204+00	Admin Master	2026-03-15 23:06:45.083204+00	Admin Master	Equipament 1	34f377a9-a6cc-4868-ba45-48fc0e1b6228	\N	\N
d5f25677-0686-4c9e-bcf4-4996d80d13c3	t	2026-03-15 23:14:29.285359+00	Admin Master	2026-03-15 23:14:29.285359+00	Admin Master	Conjunct 2	34f377a9-a6cc-4868-ba45-48fc0e1b6228	01484413-6789-40b1-8d95-af53b235fc8b	\N
\.


--
-- TOC entry 3503 (class 0 OID 16422)
-- Dependencies: 223
-- Data for Name: equipament_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.equipament_table (id, active, created_at, created_by, updated_at, updated_by, maximum_capacity, model, number_axles, plate, renavam) FROM stdin;
34f377a9-a6cc-4868-ba45-48fc0e1b6228	t	2026-03-15 23:00:40.777364+00	Admin Master	2026-03-15 23:00:40.777364+00	Admin Master	30	Cavalo	\N	ERP-0D22	12345678910
01484413-6789-40b1-8d95-af53b235fc8b	t	2026-03-15 23:13:12.143619+00	Admin Master	2026-03-15 23:13:12.143619+00	Admin Master	30	Carreta	\N	CZC-3363	12354689109
\.


--
-- TOC entry 3507 (class 0 OID 16540)
-- Dependencies: 227
-- Data for Name: telemetry_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.telemetry_table (id, alert, data1, data2, date_time, device, latitude, longitude, speed, equipament_id) FROM stdin;
\.


--
-- TOC entry 3504 (class 0 OID 16431)
-- Dependencies: 224
-- Data for Name: tractor_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.tractor_table (type_fuel, id) FROM stdin;
\.


--
-- TOC entry 3505 (class 0 OID 16437)
-- Dependencies: 225
-- Data for Name: trailer_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.trailer_table (maximum_volume, id) FROM stdin;
100	01484413-6789-40b1-8d95-af53b235fc8b
\.


--
-- TOC entry 3506 (class 0 OID 16443)
-- Dependencies: 226
-- Data for Name: user_table; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.user_table (id, active, created_at, created_by, updated_at, updated_by, cpf, email, name, password, user_profile, company_id) FROM stdin;
c916e36f-4846-41be-9b32-9e0ff8850a29	t	2026-03-15 22:23:57.149323+00	\N	2026-03-15 22:23:57.149323+00	\N	000.000.000-00	admin@admin.com	Admin Master	$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi	ADMIN	d9d7b435-c256-405b-877c-848f4a22e22a
438eca76-a0cf-480b-8c85-f129ec3e9216	t	2026-03-15 23:04:31.316347+00	Admin Master	2026-03-15 23:04:31.316347+00	Admin Master	49018326895	jonathan@admin.com	Jonathan Alves	$2a$10$dSXyVxyZSainREynEB9SD.Lu1VNUkpvAtYukS0sns6JbAS8W7Sa6e	DRIVER	d9d7b435-c256-405b-877c-848f4a22e22a
\.


--
-- TOC entry 3322 (class 2606 OID 16393)
-- Name: address_table address_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.address_table
    ADD CONSTRAINT address_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3324 (class 2606 OID 16402)
-- Name: company_table company_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT company_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3328 (class 2606 OID 16411)
-- Name: driver_table driver_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT driver_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3332 (class 2606 OID 16421)
-- Name: equipament_group_table equipament_group_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT equipament_group_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3334 (class 2606 OID 16430)
-- Name: equipament_table equipament_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_table
    ADD CONSTRAINT equipament_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3342 (class 2606 OID 16552)
-- Name: telemetry_table telemetry_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT telemetry_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3336 (class 2606 OID 16436)
-- Name: tractor_table tractor_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT tractor_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3338 (class 2606 OID 16442)
-- Name: trailer_table trailer_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT trailer_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3326 (class 2606 OID 16454)
-- Name: company_table ukd521k4bkmmpvykqinw8y2xll4; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT ukd521k4bkmmpvykqinw8y2xll4 UNIQUE (address_id);


--
-- TOC entry 3330 (class 2606 OID 16456)
-- Name: driver_table ukljh0t57m3aq70di1sr03mkamm; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT ukljh0t57m3aq70di1sr03mkamm UNIQUE (user_id);


--
-- TOC entry 3340 (class 2606 OID 16452)
-- Name: user_table user_table_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT user_table_pkey PRIMARY KEY (id);


--
-- TOC entry 3343 (class 2606 OID 16457)
-- Name: company_table fk3pinakiucrrpni74drxw40wry; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.company_table
    ADD CONSTRAINT fk3pinakiucrrpni74drxw40wry FOREIGN KEY (address_id) REFERENCES public.address_table(id);


--
-- TOC entry 3351 (class 2606 OID 16553)
-- Name: telemetry_table fk8agm95f2i43eipl5gct1cooys; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.telemetry_table
    ADD CONSTRAINT fk8agm95f2i43eipl5gct1cooys FOREIGN KEY (equipament_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3350 (class 2606 OID 16492)
-- Name: user_table fk8fosf57y9aqnb15l1sp7v6hx9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.user_table
    ADD CONSTRAINT fk8fosf57y9aqnb15l1sp7v6hx9 FOREIGN KEY (company_id) REFERENCES public.company_table(id);


--
-- TOC entry 3345 (class 2606 OID 16467)
-- Name: equipament_group_table fkh75qjn0g3oegn5ah11dkjswtp; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkh75qjn0g3oegn5ah11dkjswtp FOREIGN KEY (equipament1_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3346 (class 2606 OID 16477)
-- Name: equipament_group_table fkjrnonengpn01rsb9hgvsfufwn; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkjrnonengpn01rsb9hgvsfufwn FOREIGN KEY (equipament3_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3347 (class 2606 OID 16472)
-- Name: equipament_group_table fkmnl5l7kynnf1te95f6qdmf7y5; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.equipament_group_table
    ADD CONSTRAINT fkmnl5l7kynnf1te95f6qdmf7y5 FOREIGN KEY (equipament2_id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3344 (class 2606 OID 16462)
-- Name: driver_table fkq31c4b12e6xftex24003i1qqd; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.driver_table
    ADD CONSTRAINT fkq31c4b12e6xftex24003i1qqd FOREIGN KEY (user_id) REFERENCES public.user_table(id);


--
-- TOC entry 3349 (class 2606 OID 16487)
-- Name: trailer_table fkte47awk3lga1d0faeec89bhye; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.trailer_table
    ADD CONSTRAINT fkte47awk3lga1d0faeec89bhye FOREIGN KEY (id) REFERENCES public.equipament_table(id);


--
-- TOC entry 3348 (class 2606 OID 16482)
-- Name: tractor_table fktpyy3ovef9vtfaubofrwkr6w1; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tractor_table
    ADD CONSTRAINT fktpyy3ovef9vtfaubofrwkr6w1 FOREIGN KEY (id) REFERENCES public.equipament_table(id);


-- Completed on 2026-04-08 01:27:38 UTC

--
-- PostgreSQL database dump complete
--

\unrestrict sgXnLKRZDIjZqk1IKEG9bAe6SrTIsSMYvtTlEgNoBkYIyDz73kemg6rtJaBDaYl

