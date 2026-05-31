-- SQL setup for Server-Side Wallet & Tournament Logic
-- Run this in your Supabase SQL Editor

-- 0. SCHEMA CREATION
-- Create tables if they don't exist
CREATE TABLE IF NOT EXISTS public.users (
    id uuid REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    username text,
    balance numeric DEFAULT 0,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.transactions (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id uuid REFERENCES public.users(id) ON DELETE CASCADE,
    amount numeric NOT NULL,
    type text NOT NULL,
    "timestamp" timestamptz DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.tournaments (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    title text NOT NULL,
    entry_fee numeric DEFAULT 0,
    filled_slots int DEFAULT 0,
    max_slots int DEFAULT 100,
    status text DEFAULT 'upcoming',
    created_at timestamptz DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.tournament_results (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    tournament_id uuid REFERENCES public.tournaments(id) ON DELETE CASCADE,
    user_id uuid REFERENCES public.users(id) ON DELETE CASCADE,
    placement int DEFAULT 0,
    score numeric,
    reward_amount numeric DEFAULT 0,
    created_at timestamptz DEFAULT now(),
    UNIQUE(tournament_id, user_id)
);

-- Trigger to automatically create a public.users row when an auth.users signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
    INSERT INTO public.users (id, username, balance)
    VALUES (new.id, new.email, 0)
    ON CONFLICT (id) DO NOTHING;
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop trigger if exists, then recreate
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- Enable RLS on tables but allow authenticated users to read. 
-- Security Definer RPCs bypass RLS to perform writes.
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tournaments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tournament_results ENABLE ROW LEVEL SECURITY;

-- Allow users to view their own data
CREATE POLICY "Users can view own data" ON public.users FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can view own transactions" ON public.transactions FOR SELECT USING (auth.uid() = user_id);
-- Allow everyone to view tournaments
CREATE POLICY "Anyone can view tournaments" ON public.tournaments FOR SELECT USING (true);
CREATE POLICY "Anyone can view results" ON public.tournament_results FOR SELECT USING (true);


-- 1. Add Funds RPC
CREATE OR REPLACE FUNCTION add_funds(p_amount numeric)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be greater than zero';
    END IF;

    -- Ensure user row exists, just in case trigger missed
    INSERT INTO public.users (id, balance) VALUES (auth.uid(), 0) ON CONFLICT (id) DO NOTHING;

    UPDATE public.users
    SET balance = balance + p_amount,
        updated_at = now()
    WHERE id = auth.uid();

    INSERT INTO public.transactions (user_id, amount, type, "timestamp")
    VALUES (auth.uid(), p_amount, 'deposit', now());
END;
$$;

-- 2. Withdraw Funds RPC
CREATE OR REPLACE FUNCTION withdraw_funds(p_amount numeric)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    current_balance numeric;
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be greater than zero';
    END IF;

    SELECT balance INTO current_balance
    FROM public.users
    WHERE id = auth.uid() FOR UPDATE;

    IF current_balance IS NULL THEN
        RAISE EXCEPTION 'User profile not found';
    END IF;

    IF current_balance < p_amount THEN
        RAISE EXCEPTION 'Insufficient balance';
    END IF;

    UPDATE public.users
    SET balance = balance - p_amount,
        updated_at = now()
    WHERE id = auth.uid();

    INSERT INTO public.transactions (user_id, amount, type, "timestamp")
    VALUES (auth.uid(), p_amount, 'withdrawal', now());
END;
$$;

-- 3. Join Tournament RPC (Accepts text to avoid UUID parsing errors from client)
CREATE OR REPLACE FUNCTION join_tournament(p_tournament_id text)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_entry_fee numeric;
    current_balance numeric;
    v_filled_slots int;
    v_max_slots int;
    v_status text;
    v_uuid uuid := p_tournament_id::uuid;
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    -- Get tournament details
    SELECT entry_fee, filled_slots, max_slots, status 
    INTO v_entry_fee, v_filled_slots, v_max_slots, v_status
    FROM public.tournaments
    WHERE id = v_uuid FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Tournament not found';
    END IF;

    IF v_status != 'upcoming' THEN
        RAISE EXCEPTION 'Tournament is not upcoming';
    END IF;

    IF v_filled_slots >= v_max_slots THEN
        RAISE EXCEPTION 'Tournament is full';
    END IF;

    IF EXISTS (SELECT 1 FROM public.tournament_results WHERE tournament_id = v_uuid AND user_id = auth.uid()) THEN
        RAISE EXCEPTION 'Already joined this tournament';
    END IF;

    -- Check balance
    SELECT balance INTO current_balance
    FROM public.users
    WHERE id = auth.uid() FOR UPDATE;

    IF current_balance IS NULL THEN
        RAISE EXCEPTION 'User profile not found';
    END IF;

    IF current_balance < v_entry_fee THEN
        RAISE EXCEPTION 'Insufficient balance';
    END IF;

    -- Deduct balance
    UPDATE public.users
    SET balance = balance - v_entry_fee,
        updated_at = now()
    WHERE id = auth.uid();

    -- Add transaction
    INSERT INTO public.transactions (user_id, amount, type, "timestamp")
    VALUES (auth.uid(), v_entry_fee, 'entry_fee', now());

    -- Add to tournament_results to mark as registered
    INSERT INTO public.tournament_results (tournament_id, user_id, placement, score, reward_amount)
    VALUES (v_uuid, auth.uid(), 0, null, 0);

    -- Update tournament slots
    UPDATE public.tournaments
    SET filled_slots = filled_slots + 1
    WHERE id = v_uuid;

END;
$$;

-- 4. Update Profile RPC
CREATE OR REPLACE FUNCTION update_profile(p_username text)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    UPDATE public.users
    SET username = p_username,
        updated_at = now()
    WHERE id = auth.uid();
END;
$$;

-- Complete Security Hardening for public schema functions
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA public FROM anon, authenticated, public;

-- Permissions: Allow logged-in users (authenticated role) to call
GRANT EXECUTE ON FUNCTION public.add_funds(numeric) TO authenticated;
GRANT EXECUTE ON FUNCTION public.withdraw_funds(numeric) TO authenticated;
GRANT EXECUTE ON FUNCTION public.join_tournament(text) TO authenticated;
GRANT EXECUTE ON FUNCTION public.update_profile(text) TO authenticated;
