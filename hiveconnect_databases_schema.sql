--
-- PostgreSQL database cluster dump
--

SET default_transaction_read_only = off;

SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE apollo;
ALTER ROLE apollo WITH SUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN NOREPLICATION NOBYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:npuwbfT4pIQyAGxJFWTo7Q==$7gFw35LEpJkLqwde3MLdD62orC63IKAMqMhwGYbi0g0=:39FQC9gX2jtaaFh0rL8yIpwHFyNjkYWmVU/Hpw69PiE=';
CREATE ROLE ipamuser;
ALTER ROLE ipamuser WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN NOREPLICATION NOBYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:T7aLBwagvsaRE34Xwx6RZA==$IYRCgaJIfUBBpAXPLSVnq07MC8Sge9cG2QCP7SN9fKo=:0x8Pk7OKUg3Oi9ydtkm/frgxvlAet9LO+4ErNFwDl6w=';
CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:y3AioOz0wvTZY01BSEYe3g==$RxI5ryCMLnBE5ifFSg6SQcE7sv7ruw6JjK1xDWjZp2g=:VeZhRRvXH2ANHkM6C2qitRtyM2RTS6bNQtLN7CHpidQ=';






--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

--
-- Database "hiveDB" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: hiveDB; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "hiveDB" WITH TEMPLATE = template0 ENCODING = 'SQL_ASCII' LOCALE = 'C';


ALTER DATABASE "hiveDB" OWNER TO postgres;

\connect "hiveDB"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: hive_clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hive_clients (
    id bigint NOT NULL,
    account_no character varying(255) DEFAULT NULL::character varying,
    backend character varying(255) DEFAULT NULL::character varying,
    client_name character varying(255) DEFAULT NULL::character varying,
    ip_assigned character varying(255) DEFAULT NULL::character varying,
    olt_interface character varying(255) DEFAULT NULL::character varying,
    olt_ip character varying(255) DEFAULT NULL::character varying,
    olt_downstream character varying(255) DEFAULT NULL::character varying,
    olt_upstream character varying(255) DEFAULT NULL::character varying,
    subscription_name character varying(255) DEFAULT NULL::character varying,
    modem_mac_address character varying(255) DEFAULT NULL::character varying,
    onu_serial_number character varying(255) DEFAULT NULL::character varying,
    package_type character varying(255) DEFAULT NULL::character varying,
    area_id_site integer,
    ssid_name character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.hive_clients OWNER TO postgres;

--
-- Name: hive_clients_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hive_clients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hive_clients_id_seq OWNER TO postgres;

--
-- Name: hive_clients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.hive_clients_id_seq OWNED BY public.hive_clients.id;


--
-- Name: hive_clients_seq; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hive_clients_seq (
    next_val bigint
);


ALTER TABLE public.hive_clients_seq OWNER TO postgres;

--
-- Name: hive_clients id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hive_clients ALTER COLUMN id SET DEFAULT nextval('public.hive_clients_id_seq'::regclass);


--
-- Name: hive_clients hive_clients_ip_assigned_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hive_clients
    ADD CONSTRAINT hive_clients_ip_assigned_key UNIQUE (ip_assigned);


--
-- Name: hive_clients hive_clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hive_clients
    ADD CONSTRAINT hive_clients_pkey PRIMARY KEY (id);


--
-- Name: DATABASE "hiveDB"; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON DATABASE "hiveDB" TO apollo;


--
-- PostgreSQL database dump complete
--

--
-- Database "ipamDB" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: ipamDB; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "ipamDB" WITH TEMPLATE = template0 ENCODING = 'SQL_ASCII' LOCALE = 'C';


ALTER DATABASE "ipamDB" OWNER TO postgres;

\connect "ipamDB"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cidr_block; Type: TABLE; Schema: public; Owner: apollo
--

CREATE TABLE public.cidr_block (
    cidr_id bigint NOT NULL,
    broadcast_address character varying(255) NOT NULL,
    cidr_block character varying(255) NOT NULL,
    default_gateway character varying(255) NOT NULL,
    network_address character varying(255) NOT NULL,
    network_name character varying(255),
    network_type character varying(255),
    vlan_id character varying(255),
    site_location character varying(255)
);


ALTER TABLE public.cidr_block OWNER TO apollo;

--
-- Name: cidr_block_cidr_id_seq; Type: SEQUENCE; Schema: public; Owner: apollo
--

CREATE SEQUENCE public.cidr_block_cidr_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cidr_block_cidr_id_seq OWNER TO apollo;

--
-- Name: cidr_block_cidr_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: apollo
--

ALTER SEQUENCE public.cidr_block_cidr_id_seq OWNED BY public.cidr_block.cidr_id;


--
-- Name: cidr_ipaddress; Type: TABLE; Schema: public; Owner: apollo
--

CREATE TABLE public.cidr_ipaddress (
    id bigint NOT NULL,
    accountnumber character varying(255),
    ipaddress character varying(255),
    networkaddress character varying(255),
    notes character varying(255),
    status character varying(255),
    type character varying(255),
    vlanid character varying(255)
);


ALTER TABLE public.cidr_ipaddress OWNER TO apollo;

--
-- Name: cidr_ipaddress_id_seq; Type: SEQUENCE; Schema: public; Owner: apollo
--

CREATE SEQUENCE public.cidr_ipaddress_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cidr_ipaddress_id_seq OWNER TO apollo;

--
-- Name: cidr_ipaddress_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: apollo
--

ALTER SEQUENCE public.cidr_ipaddress_id_seq OWNED BY public.cidr_ipaddress.id;


--
-- Name: cidr_block cidr_id; Type: DEFAULT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_block ALTER COLUMN cidr_id SET DEFAULT nextval('public.cidr_block_cidr_id_seq'::regclass);


--
-- Name: cidr_ipaddress id; Type: DEFAULT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_ipaddress ALTER COLUMN id SET DEFAULT nextval('public.cidr_ipaddress_id_seq'::regclass);


--
-- Name: cidr_block cidr_block_broadcast_address_key; Type: CONSTRAINT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_block
    ADD CONSTRAINT cidr_block_broadcast_address_key UNIQUE (broadcast_address);


--
-- Name: cidr_block cidr_block_cidr_block_key; Type: CONSTRAINT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_block
    ADD CONSTRAINT cidr_block_cidr_block_key UNIQUE (cidr_block);


--
-- Name: cidr_block cidr_block_default_gateway_key; Type: CONSTRAINT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_block
    ADD CONSTRAINT cidr_block_default_gateway_key UNIQUE (default_gateway);


--
-- Name: cidr_block cidr_block_network_address_key; Type: CONSTRAINT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_block
    ADD CONSTRAINT cidr_block_network_address_key UNIQUE (network_address);


--
-- Name: cidr_block cidr_block_pkey; Type: CONSTRAINT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_block
    ADD CONSTRAINT cidr_block_pkey PRIMARY KEY (cidr_id);


--
-- Name: cidr_ipaddress cidr_ipaddress_pkey; Type: CONSTRAINT; Schema: public; Owner: apollo
--

ALTER TABLE ONLY public.cidr_ipaddress
    ADD CONSTRAINT cidr_ipaddress_pkey PRIMARY KEY (id);


--
-- Name: DATABASE "ipamDB"; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON DATABASE "ipamDB" TO apollo;


--
-- PostgreSQL database dump complete
--

--
-- Database "oltDB" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: oltDB; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "oltDB" WITH TEMPLATE = template0 ENCODING = 'SQL_ASCII' LOCALE = 'C';


ALTER DATABASE "oltDB" OWNER TO postgres;

\connect "oltDB"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: site_olt; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.site_olt (
    olt_id bigint NOT NULL,
    olt_ipaddress character varying(255) NOT NULL,
    olt_name character varying(255) NOT NULL,
    olt_network_site character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.site_olt OWNER TO postgres;

--
-- Name: site_olt_olt_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.site_olt_olt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.site_olt_olt_id_seq OWNER TO postgres;

--
-- Name: site_olt_olt_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.site_olt_olt_id_seq OWNED BY public.site_olt.olt_id;


--
-- Name: site_olt olt_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.site_olt ALTER COLUMN olt_id SET DEFAULT nextval('public.site_olt_olt_id_seq'::regclass);


--
-- Name: site_olt site_olt_olt_ipaddress_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.site_olt
    ADD CONSTRAINT site_olt_olt_ipaddress_key UNIQUE (olt_ipaddress);


--
-- Name: site_olt site_olt_olt_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.site_olt
    ADD CONSTRAINT site_olt_olt_name_key UNIQUE (olt_name);


--
-- Name: site_olt site_olt_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.site_olt
    ADD CONSTRAINT site_olt_pkey PRIMARY KEY (olt_id);


--
-- Name: DATABASE "oltDB"; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON DATABASE "oltDB" TO apollo;


--
-- PostgreSQL database dump complete
--

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

--
-- Database "subscriberDB" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.12 (Ubuntu 14.12-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: subscriberDB; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "subscriberDB" WITH TEMPLATE = template0 ENCODING = 'SQL_ASCII' LOCALE = 'C';


ALTER DATABASE "subscriberDB" OWNER TO postgres;

\connect "subscriberDB"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: hive_clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hive_clients (
    id bigint NOT NULL,
    account_no character varying(255) DEFAULT NULL::character varying,
    backend character varying(255) DEFAULT NULL::character varying,
    client_name character varying(255) DEFAULT NULL::character varying,
    ip_assigned character varying(255) DEFAULT NULL::character varying,
    olt_interface character varying(255) DEFAULT NULL::character varying,
    olt_ip character varying(255) DEFAULT NULL::character varying,
    olt_downstream character varying(255) DEFAULT NULL::character varying,
    olt_upstream character varying(255) DEFAULT NULL::character varying,
    subscription_name character varying(255) DEFAULT NULL::character varying,
    modem_mac_address character varying(255) DEFAULT NULL::character varying,
    onu_serial_number character varying(255) DEFAULT NULL::character varying,
    package_type character varying(255) DEFAULT NULL::character varying,
    area_id_site integer,
    ssid_name character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.hive_clients OWNER TO postgres;

--
-- Name: hive_clients_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hive_clients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hive_clients_id_seq OWNER TO postgres;

--
-- Name: hive_clients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.hive_clients_id_seq OWNED BY public.hive_clients.id;


--
-- Name: hive_clients_seq; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hive_clients_seq (
    next_val bigint
);


ALTER TABLE public.hive_clients_seq OWNER TO postgres;

--
-- Name: new_packages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.new_packages (
    id bigint NOT NULL,
    cir character varying(255) NOT NULL,
    package_type character varying(255) NOT NULL,
    max_speed character varying(255) NOT NULL
);


ALTER TABLE public.new_packages OWNER TO postgres;

--
-- Name: new_packages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.new_packages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.new_packages_id_seq OWNER TO postgres;

--
-- Name: new_packages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.new_packages_id_seq OWNED BY public.new_packages.id;


--
-- Name: new_subscriber; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.new_subscriber (
    subscriber_id bigint NOT NULL,
    account_number character varying(255) NOT NULL,
    bucket_id character varying(255) DEFAULT NULL::character varying,
    ip_assigned character varying(255) DEFAULT NULL::character varying,
    subscription_name character varying(255) DEFAULT NULL::character varying,
    modem_mac_address character varying(255) DEFAULT NULL::character varying,
    onu_serial_number character varying(255) DEFAULT NULL::character varying,
    package_type character varying(255) DEFAULT NULL::character varying,
    package_id character varying(255) DEFAULT NULL::character varying,
    provision_type character varying(255) DEFAULT NULL::character varying,
    area_id character varying(255) DEFAULT NULL::character varying,
    subscriber_name character varying(255) NOT NULL,
    subscriber_status character varying(255) DEFAULT NULL::character varying,
    olt_ip character varying(255) DEFAULT NULL::character varying,
    olt_downstream character varying(255) DEFAULT NULL::character varying,
    olt_upstream character varying(255) DEFAULT NULL::character varying,
    ssid_name character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.new_subscriber OWNER TO postgres;

--
-- Name: new_subscriber_subscriber_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.new_subscriber_subscriber_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.new_subscriber_subscriber_id_seq OWNER TO postgres;

--
-- Name: new_subscriber_subscriber_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.new_subscriber_subscriber_id_seq OWNED BY public.new_subscriber.subscriber_id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying(20),
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_MODERATOR'::character varying, 'ROLE_ADMIN'::character varying])::text[])))
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.roles_id_seq OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id integer NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255),
    password character varying(255),
    username character varying(255)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: hive_clients id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hive_clients ALTER COLUMN id SET DEFAULT nextval('public.hive_clients_id_seq'::regclass);


--
-- Name: new_packages id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_packages ALTER COLUMN id SET DEFAULT nextval('public.new_packages_id_seq'::regclass);


--
-- Name: new_subscriber subscriber_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_subscriber ALTER COLUMN subscriber_id SET DEFAULT nextval('public.new_subscriber_subscriber_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: hive_clients hive_clients_ip_assigned_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hive_clients
    ADD CONSTRAINT hive_clients_ip_assigned_key UNIQUE (ip_assigned);


--
-- Name: hive_clients hive_clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hive_clients
    ADD CONSTRAINT hive_clients_pkey PRIMARY KEY (id);


--
-- Name: new_packages new_packages_package_type_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_packages
    ADD CONSTRAINT new_packages_package_type_key UNIQUE (package_type);


--
-- Name: new_packages new_packages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_packages
    ADD CONSTRAINT new_packages_pkey PRIMARY KEY (id);


--
-- Name: new_subscriber new_subscriber_account_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_subscriber
    ADD CONSTRAINT new_subscriber_account_number_key UNIQUE (account_number);


--
-- Name: new_subscriber new_subscriber_ip_assigned_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_subscriber
    ADD CONSTRAINT new_subscriber_ip_assigned_key UNIQUE (ip_assigned);


--
-- Name: new_subscriber new_subscriber_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.new_subscriber
    ADD CONSTRAINT new_subscriber_pkey PRIMARY KEY (subscriber_id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: users ukr43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: DATABASE "subscriberDB"; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON DATABASE "subscriberDB" TO apollo;


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database cluster dump complete
--

