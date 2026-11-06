package io.truthchain.dao;

import io.truthchain.config.Database;
import java.sql.*;
import java.time.LocalDate;

public class SettingsDao {
    public LocalDate quantizationDate()throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT setting_value FROM app_settings WHERE setting_key='quantization_date'");ResultSet r=p.executeQuery()){return r.next()?LocalDate.parse(r.getString(1)):LocalDate.now().minusDays(7);}
    }
    public void setQuantizationDate(LocalDate date)throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO app_settings(setting_key,setting_value) VALUES('quantization_date',?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)")){p.setString(1,date.toString());p.executeUpdate();}
    }
}
