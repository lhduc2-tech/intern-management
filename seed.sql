-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.attendance (
  id uuid NOT NULL,
  intern_id uuid NOT NULL,
  date date,
  note text,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT attendance_pkey PRIMARY KEY (id),
  CONSTRAINT attendance_intern_id_fkey FOREIGN KEY (intern_id) REFERENCES public.interns(id)
);
CREATE TABLE public.final_results (
  id uuid NOT NULL,
  intern_id uuid NOT NULL UNIQUE,
  result character varying,
  note text,
  approved_by uuid,
  approved_at timestamp with time zone,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT final_results_pkey PRIMARY KEY (id),
  CONSTRAINT final_results_intern_id_fkey FOREIGN KEY (intern_id) REFERENCES public.interns(id),
  CONSTRAINT final_results_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.users(id)
);
CREATE TABLE public.intern_project (
  intern_id uuid NOT NULL,
  project_id uuid NOT NULL,
  joined_at timestamp with time zone DEFAULT now(),
  role character varying,
  CONSTRAINT intern_project_pkey PRIMARY KEY (intern_id, project_id),
  CONSTRAINT intern_project_intern_id_fkey FOREIGN KEY (intern_id) REFERENCES public.interns(id),
  CONSTRAINT intern_project_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id)
);
CREATE TABLE public.interns (
  id uuid NOT NULL,
  user_id uuid UNIQUE,
  status character varying NOT NULL DEFAULT 'ACTIVE'::character varying,
  is_graduated boolean DEFAULT false,
  company_account character varying UNIQUE,
  technology character varying,
  mentor_id uuid,
  organization character varying,
  gender character varying,
  date_of_birth date,
  identity_number character varying UNIQUE,
  identity_issue_date date,
  level character varying,
  major character varying,
  university character varying,
  phone character varying,
  permanent_address character varying,
  position character varying,
  internship_start_date date,
  internship_end_date date,
  acceptance_document character varying,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  internship_session_id uuid,
  CONSTRAINT interns_pkey PRIMARY KEY (id),
  CONSTRAINT interns_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id),
  CONSTRAINT interns_mentor_id_fkey FOREIGN KEY (mentor_id) REFERENCES public.mentors(id),
  CONSTRAINT interns_internship_session_id_fk FOREIGN KEY (internship_session_id) REFERENCES public.internship_session(id)
);
CREATE TABLE public.internship_session (
  id uuid NOT NULL,
  name character varying,
  start_date date,
  end_date date,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  total_intern integer,
  CONSTRAINT internship_session_pkey PRIMARY KEY (id)
);
CREATE TABLE public.mentors (
  id uuid NOT NULL,
  user_id uuid UNIQUE,
  specialization character varying,
  status character varying DEFAULT 'ACTIVE'::character varying,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT mentors_pkey PRIMARY KEY (id),
  CONSTRAINT mentors_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.permissions (
  id uuid NOT NULL,
  code character varying NOT NULL UNIQUE,
  description text,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT permissions_pkey PRIMARY KEY (id)
);
CREATE TABLE public.projects (
  id uuid NOT NULL,
  name character varying NOT NULL,
  description text,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT projects_pkey PRIMARY KEY (id)
);
CREATE TABLE public.report_evaluation (
  id uuid NOT NULL,
  report_id uuid NOT NULL,
  reviewer_id uuid,
  score numeric,
  feedback text,
  status character varying DEFAULT 'REVIEWED'::character varying,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT report_evaluation_pkey PRIMARY KEY (id),
  CONSTRAINT report_evaluation_report_id_fkey FOREIGN KEY (report_id) REFERENCES public.reports(id),
  CONSTRAINT report_evaluation_reviewer_id_fkey FOREIGN KEY (reviewer_id) REFERENCES public.users(id)
);
CREATE TABLE public.reports (
  id uuid NOT NULL,
  intern_id uuid NOT NULL,
  title character varying,
  content text,
  file_url character varying,
  submitted_at timestamp with time zone,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  status character varying DEFAULT 'SUBMITTED'::character varying,
  CONSTRAINT reports_pkey PRIMARY KEY (id),
  CONSTRAINT reports_intern_id_fkey FOREIGN KEY (intern_id) REFERENCES public.interns(id)
);
CREATE TABLE public.role_permission (
  role_id uuid NOT NULL,
  permission_id uuid NOT NULL,
  CONSTRAINT role_permission_pkey PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES public.roles(id),
  CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES public.permissions(id)
);
CREATE TABLE public.roles (
  id uuid NOT NULL,
  code character varying NOT NULL UNIQUE,
  name character varying,
  description text,
  status character varying NOT NULL DEFAULT 'ACTIVE'::character varying,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT roles_pkey PRIMARY KEY (id)
);
CREATE TABLE public.task_assignment (
  id uuid NOT NULL,
  task_id uuid NOT NULL,
  intern_id uuid NOT NULL,
  assigned_at timestamp with time zone,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT task_assignment_pkey PRIMARY KEY (id),
  CONSTRAINT task_assignment_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id),
  CONSTRAINT task_assignment_intern_id_fkey FOREIGN KEY (intern_id) REFERENCES public.interns(id)
);
CREATE TABLE public.task_progress (
  id uuid NOT NULL,
  task_id uuid NOT NULL,
  intern_id uuid NOT NULL,
  progress integer CHECK (progress >= 0 AND progress <= 100),
  note text,
  updated_at timestamp with time zone,
  updated_by character varying,
  created_at timestamp with time zone,
  created_by character varying,
  CONSTRAINT task_progress_pkey PRIMARY KEY (id),
  CONSTRAINT task_progress_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id),
  CONSTRAINT task_progress_intern_id_fkey FOREIGN KEY (intern_id) REFERENCES public.interns(id)
);
CREATE TABLE public.tasks (
  id uuid NOT NULL,
  project_id uuid NOT NULL,
  title character varying,
  description text,
  priority character varying,
  start_date date,
  due_date date,
  status character varying DEFAULT 'TODO'::character varying,
  created_at timestamp with time zone,
  created_by character varying,
  updated_at timestamp with time zone,
  updated_by character varying,
  CONSTRAINT tasks_pkey PRIMARY KEY (id),
  CONSTRAINT tasks_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id)
);
CREATE TABLE public.user_role (
  user_id uuid NOT NULL,
  role_id uuid NOT NULL,
  CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES public.users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES public.roles(id)
);
CREATE TABLE public.users (
  id uuid NOT NULL,
  username character varying NOT NULL UNIQUE,
  email character varying NOT NULL UNIQUE,
  password_hash character varying NOT NULL,
  status character varying NOT NULL DEFAULT 'ACTIVE'::character varying,
  last_login_at timestamp with time zone,
  created_at timestamp with time zone,
  updated_at timestamp with time zone,
  created_by character varying,
  updated_by character varying,
  full_name character varying,
  CONSTRAINT users_pkey PRIMARY KEY (id)
);
