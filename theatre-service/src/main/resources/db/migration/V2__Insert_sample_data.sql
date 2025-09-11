-- Insert sample cities
INSERT INTO cities (name, state, country, zip_code) VALUES
('Mumbai', 'Maharashtra', 'India', '400001'),
('Delhi', 'Delhi', 'India', '110001'),
('Bangalore', 'Karnataka', 'India', '560001'),
('Chennai', 'Tamil Nadu', 'India', '600001'),
('Kolkata', 'West Bengal', 'India', '700001'),
('Hyderabad', 'Telangana', 'India', '500001'),
('Pune', 'Maharashtra', 'India', '411001'),
('Ahmedabad', 'Gujarat', 'India', '380001'),
('Surat', 'Gujarat', 'India', '395001'),
('Jaipur', 'Rajasthan', 'India', '302001');

-- Insert sample theatres
INSERT INTO theatres (name, address, phone_number, email, latitude, longitude, city_id) VALUES
-- Mumbai theatres
('INOX R-City Mall', 'R-City Mall, Ghatkopar West, Mumbai', '+91-22-67890123', 'rcity@inoxmovies.com', 19.0862, 72.9093, 1),
('PVR Phoenix Mills', 'Phoenix Mills, Lower Parel, Mumbai', '+91-22-24305667', 'phoenix@pvr.co.in', 19.0135, 72.8298, 1),
('Cinepolis Fun Republic', 'Fun Republic Mall, Andheri West, Mumbai', '+91-22-40001234', 'andheri@cinepolis.co.in', 19.1368, 72.8269, 1),

-- Delhi theatres
('PVR Select City Walk', 'Select City Walk, Saket, New Delhi', '+91-11-29565000', 'saket@pvr.co.in', 28.5245, 77.2066, 2),
('INOX Nehru Place', 'Nehru Place, New Delhi', '+91-11-26228800', 'nehruplace@inoxmovies.com', 28.5506, 77.2506, 2),
('DT Cinemas DLF Mall', 'DLF Mall of India, Noida', '+91-120-4567890', 'dlf@dtcinemas.com', 28.5677, 77.3250, 2),

-- Bangalore theatres
('INOX Garuda Mall', 'Garuda Mall, Magrath Road, Bangalore', '+91-80-25599000', 'garuda@inoxmovies.com', 12.9716, 77.5946, 3),
('PVR Forum Mall', 'Forum Mall, Koramangala, Bangalore', '+91-80-41127300', 'forum@pvr.co.in', 12.9279, 77.6271, 3),
('Cinepolis Royal Meenakshi', 'Royal Meenakshi Mall, Bannerghatta Road, Bangalore', '+91-80-67890123', 'royal@cinepolis.co.in', 12.8955, 77.5937, 3),

-- Chennai theatres
('INOX Express Avenue', 'Express Avenue Mall, Royapettah, Chennai', '+91-44-28578800', 'express@inoxmovies.com', 13.0569, 80.2570, 4),
('PVR Ampa Skywalk', 'Ampa Skywalk, Aminjikarai, Chennai', '+91-44-28343400', 'ampa@pvr.co.in', 13.0732, 80.2206, 4);

-- Insert sample screens
INSERT INTO screens (name, total_seats, screen_type, theatre_id) VALUES
-- INOX R-City Mall screens
('Screen 1', 180, 'IMAX', 1),
('Screen 2', 150, 'DOLBY_ATMOS', 1),
('Screen 3', 120, 'STANDARD', 1),
('Screen 4', 100, 'RECLINER', 1),

-- PVR Phoenix Mills screens
('Audi 1', 200, 'IMAX', 2),
('Audi 2', 160, 'VIP', 2),
('Audi 3', 140, 'STANDARD', 2),

-- Cinepolis Fun Republic screens
('Screen A', 180, 'STANDARD', 3),
('Screen B', 160, 'PREMIUM', 3),
('Screen C', 120, 'VIP', 3),

-- PVR Select City Walk screens
('Gold 1', 80, 'VIP', 4),
('Gold 2', 80, 'VIP', 4),
('Screen 1', 200, 'IMAX', 4),
('Screen 2', 150, 'STANDARD', 4),

-- INOX Nehru Place screens
('Screen 1', 180, 'STANDARD', 5),
('Screen 2', 160, 'DOLBY_ATMOS', 5),
('Screen 3', 140, 'STANDARD', 5),

-- Add screens for other theatres
('Screen 1', 200, 'IMAX', 6),
('Screen 2', 180, 'STANDARD', 6),
('Screen 1', 220, 'IMAX', 7),
('Screen 2', 180, 'VIP', 7),
('Screen 3', 160, 'STANDARD', 7),
('Screen 1', 200, 'STANDARD', 8),
('Screen 2', 160, 'DOLBY_ATMOS', 8),
('Screen 1', 180, 'VIP', 9),
('Screen 2', 140, 'STANDARD', 9),
('Screen 1', 200, 'IMAX', 10),
('Screen 2', 180, 'STANDARD', 10);

-- Insert sample shows (using fictional movie IDs)
INSERT INTO shows (movie_id, movie_title, show_date_time, end_date_time, price, available_seats, status, screen_id, theatre_id) VALUES
-- Today's shows
(1, 'Avengers: Endgame', CURRENT_DATE + INTERVAL '10 hours', CURRENT_DATE + INTERVAL '13 hours', 350.00, 180, 'SCHEDULED', 1, 1),
(1, 'Avengers: Endgame', CURRENT_DATE + INTERVAL '14 hours', CURRENT_DATE + INTERVAL '17 hours', 350.00, 180, 'SCHEDULED', 1, 1),
(1, 'Avengers: Endgame', CURRENT_DATE + INTERVAL '19 hours', CURRENT_DATE + INTERVAL '22 hours', 400.00, 180, 'SCHEDULED', 1, 1),

(2, 'Spider-Man: No Way Home', CURRENT_DATE + INTERVAL '11 hours', CURRENT_DATE + INTERVAL '14 hours', 300.00, 150, 'SCHEDULED', 2, 1),
(2, 'Spider-Man: No Way Home', CURRENT_DATE + INTERVAL '16 hours', CURRENT_DATE + INTERVAL '19 hours', 300.00, 150, 'SCHEDULED', 2, 1),
(2, 'Spider-Man: No Way Home', CURRENT_DATE + INTERVAL '20 hours', CURRENT_DATE + INTERVAL '23 hours', 350.00, 150, 'SCHEDULED', 2, 1),

(3, 'The Batman', CURRENT_DATE + INTERVAL '12 hours', CURRENT_DATE + INTERVAL '15 hours', 280.00, 200, 'SCHEDULED', 5, 2),
(3, 'The Batman', CURRENT_DATE + INTERVAL '18 hours', CURRENT_DATE + INTERVAL '21 hours', 320.00, 200, 'SCHEDULED', 5, 2),

(4, 'Doctor Strange 2', CURRENT_DATE + INTERVAL '13 hours', CURRENT_DATE + INTERVAL '16 hours', 250.00, 180, 'SCHEDULED', 8, 3),
(4, 'Doctor Strange 2', CURRENT_DATE + INTERVAL '17 hours', CURRENT_DATE + INTERVAL '20 hours', 280.00, 180, 'SCHEDULED', 8, 3),

-- Tomorrow's shows
(1, 'Avengers: Endgame', CURRENT_DATE + INTERVAL '1 day 10 hours', CURRENT_DATE + INTERVAL '1 day 13 hours', 350.00, 180, 'SCHEDULED', 1, 1),
(1, 'Avengers: Endgame', CURRENT_DATE + INTERVAL '1 day 14 hours', CURRENT_DATE + INTERVAL '1 day 17 hours', 350.00, 180, 'SCHEDULED', 1, 1),
(1, 'Avengers: Endgame', CURRENT_DATE + INTERVAL '1 day 19 hours', CURRENT_DATE + INTERVAL '1 day 22 hours', 400.00, 180, 'SCHEDULED', 1, 1),

(5, 'Top Gun: Maverick', CURRENT_DATE + INTERVAL '1 day 11 hours', CURRENT_DATE + INTERVAL '1 day 14 hours', 320.00, 80, 'SCHEDULED', 10, 4),
(5, 'Top Gun: Maverick', CURRENT_DATE + INTERVAL '1 day 16 hours', CURRENT_DATE + INTERVAL '1 day 19 hours', 350.00, 80, 'SCHEDULED', 10, 4),

(6, 'Jurassic World Dominion', CURRENT_DATE + INTERVAL '1 day 12 hours', CURRENT_DATE + INTERVAL '1 day 15 hours', 300.00, 180, 'SCHEDULED', 13, 5),
(6, 'Jurassic World Dominion', CURRENT_DATE + INTERVAL '1 day 18 hours', CURRENT_DATE + INTERVAL '1 day 21 hours', 330.00, 180, 'SCHEDULED', 13, 5);

-- This will be handled by the ShowService.generateSeatAvailability method
-- But let's add a few sample seat availability records for the first show
DO $$
DECLARE
    show_record RECORD;
    seat_num INTEGER;
    row_letter CHAR(1);
    row_num INTEGER;
BEGIN
    -- Get the first show for seating
    SELECT * INTO show_record FROM shows WHERE id = 1;
    
    IF FOUND THEN
        -- Generate seats for 10 rows (A-J) with 18 seats per row = 180 total seats
        FOR row_num IN 1..10 LOOP
            row_letter := CHR(64 + row_num); -- A=65, so 64+1=65=A
            FOR seat_num IN 1..18 LOOP
                INSERT INTO seat_availability (
                    seat_number, 
                    row_number, 
                    seat_type, 
                    status, 
                    show_id
                ) VALUES (
                    row_letter || seat_num::text,
                    row_letter,
                    CASE 
                        WHEN row_num <= 2 THEN 'PREMIUM'
                        WHEN row_num >= 8 AND (seat_num = 1 OR seat_num = 18) THEN 'WHEELCHAIR_ACCESSIBLE'
                        WHEN row_num >= 7 THEN 'VIP'
                        ELSE 'REGULAR'
                    END,
                    'AVAILABLE',
                    show_record.id
                );
            END LOOP;
        END LOOP;
    END IF;
END $$;