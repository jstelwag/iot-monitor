/*
    Copyright 2015 Jaap Stelwagen

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package util;
import org.apache.commons.lang3.time.DateUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class HeatingProperties {
    public final String influxIp, localIp, logstashIp;
    public int masterPort, influxPort, localPort, logstashPort, elevation;
    public double latitude, longitude;

    public HeatingProperties()  {
        Properties prop = new Properties();
        try (InputStream inputStream = new FileInputStream("/etc/monitor.conf")) {
             prop.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        masterPort = Integer.parseInt(prop.getProperty("master.port").trim());
        influxIp = prop.getProperty("influx.ip").trim();
        influxPort = Integer.parseInt(prop.getProperty("influx.port").trim());
        logstashIp = prop.getProperty("logstash.ip").trim();
        logstashPort = Integer.parseInt(prop.getProperty("logstash.port").trim());

        localIp = prop.getProperty("local.ip").trim();
        if (prop.getProperty("local.port") != null) {
            localPort = Integer.parseInt(prop.getProperty("local.port").trim());
        } else {
            localPort = 11111;
        }
        
        latitude = Double.parseDouble(prop.getProperty("location.latitude"));
        longitude = Double.parseDouble(prop.getProperty("location.longitude"));
        elevation = Integer.parseInt(prop.getProperty("location.elevation"));
    }

    public static Date checkoutTime(Date day) {
        return DateUtils.setMinutes(DateUtils.setHours(day, 10), 0);
    }

    public static Date checkinTime(Date day) {
        return DateUtils.setMinutes(DateUtils.setHours(day, 14), 30);
    }
}
