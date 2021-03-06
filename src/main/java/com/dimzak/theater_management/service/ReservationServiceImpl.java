package com.dimzak.theater_management.service;

import com.dimzak.theater_management.model.Projection;
import com.dimzak.theater_management.model.Reservation;
import com.dimzak.theater_management.model.Theater;
import com.dimzak.theater_management.util.DataAccess;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Dimitris Zakas
 */
@ApplicationScoped
@DataAccess
public class ReservationServiceImpl implements ReservationService {

    @Resource(lookup = "java:jboss/datasources/theater_managementDS")
    private DataSource dataSource;


    @Override
    public List<Theater> getAllTheaters() {
        List<Theater> theaterList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {

            String sql = "select * from theater ;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                Theater theater = new Theater();
                theater.setTheater_id(rs.getInt("theater_id"));
                theater.setName(rs.getString("name"));
                theater.setMax_seats(rs.getInt("max_seats"));
                theaterList.add(theater);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return theaterList;
    }

    @Override
    public List<Reservation> getAllReservations() {

        List<Reservation> reservationList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {

            String sql = "select * from movies_theater ;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setTheater_id(rs.getInt("theater_id"));
                reservation.setMovies_id(rs.getInt("movies_id"));
                reservation.setView_time(rs.getTimestamp("movie_time"));
                reservation.setReservation_id(rs.getInt("movies_theater_id"));
                reservation.setReserved_seats(rs.getInt("reserved_seats"));
                reservationList.add(reservation);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return reservationList;
    }

    @Override
    public boolean bookSeat(int reservation_id) {
        boolean result = false;

        try (Connection connection = dataSource.getConnection()) {
            String sql = "update movies_theater set reserved_seats = reserved_seats + 1 WHERE movies_theater_id =" + reservation_id + " ;";
            System.out.println(sql);
            connection.createStatement().executeUpdate(sql);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }


    @Override
    public boolean reserveTheaterForMovie(Reservation reservation) {
        Boolean result = false;

        SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String currentTime = sdf.format(reservation.getView_time());

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement p = connection.prepareStatement("insert into movies_theater values (0, ?, ?, ?, null);");
            p.setString(1, currentTime);
            p.setInt(2, reservation.getTheater_id());
            p.setInt(3, reservation.getMovies_id());
            System.out.println(p.toString());
            p.executeUpdate();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Projection> displayProjections() {

        List<Projection> projections = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {

            String sql = "SELECT movies_theater.movies_theater_id, movies_theater.reserved_seats, theater.max_seats, movies_theater.movie_time, theater.name, movies.title FROM theater_management.movies_theater join theater on movies_theater.theater_id = theater.theater_id join movies on movies_theater.movies_id = movies.movies_id;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                Projection projection = new Projection();
                projection.setProjectionId(rs.getInt("movies_theater_id"));
                projection.setReserved_seats(rs.getInt("reserved_seats"));
                projection.setMax_seats(rs.getInt("max_seats"));
                projection.setView_time(rs.getTimestamp("movie_time"));
                projection.setTheater(rs.getString("name"));
                projection.setMovie(rs.getString("title"));
                projections.add(projection);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return projections;

    }

    @Override
    public List<Projection> displayProjectionsByDate(Date from, Date to) {

        SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd");

        String fromDate = sdf.format(from);
        String toDate = sdf.format(to);

        List<Projection> projections = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement p = connection.prepareStatement("SELECT movies_theater.movies_theater_id, movies_theater.reserved_seats, theater.max_seats, movies_theater.movie_time, theater.name, movies.title FROM theater_management.movies_theater join theater on movies_theater.theater_id = theater.theater_id join movies on movies_theater.movies_id = movies.movies_id" +
                    " WHERE movies_theater.movie_time BETWEEN ? AND ? ;");

            p.setString(1, fromDate);
            p.setString(2, toDate);
            System.out.println(p.toString());
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                Projection projection = new Projection();
                projection.setProjectionId(rs.getInt("movies_theater_id"));
                projection.setReserved_seats(rs.getInt("reserved_seats"));
                projection.setMax_seats(rs.getInt("max_seats"));
                projection.setView_time(rs.getTimestamp("movie_time"));
                projection.setTheater(rs.getString("name"));
                projection.setMovie(rs.getString("title"));
                projections.add(projection);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return projections;

    }
}
