package ndk.banee;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fortuna.ical4j.validate.ValidationException;

public class Main {
    public static void main(String[] args) {
        try {
            // Read the JSON file
            JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(new FileInputStream("tournaments.json")));
            JsonArray tournaments = jsonElement.getAsJsonArray();

            // Create a calendar
            Calendar calendar = new Calendar();
            calendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(CalScale.GREGORIAN);

            SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm");
            for (JsonElement element : tournaments) {
                JsonObject tournament = element.getAsJsonObject();

                String name = tournament.get("name").getAsString();
                String date = tournament.get("date").getAsString();
                String time = tournament.get("time").getAsString();

                // Create the event
                java.util.Calendar startDate = java.util.Calendar.getInstance();
                startDate.setTime(dateFormat.parse(date + "/" + startDate.get(java.util.Calendar.YEAR) + " " + time));
                DateTime start = new DateTime(startDate.getTime());
                start.setUtc(true);

                java.util.Calendar endDate = (java.util.Calendar) startDate.clone();
                endDate.add(java.util.Calendar.HOUR, 1); // assuming each event lasts 1 hour
                DateTime end = new DateTime(endDate.getTime());
                end.setUtc(true);

                VEvent event = new VEvent(start, end, name);

                // Add a daily recurrence rule
                Recur recur = new Recur(Recur.DAILY, null);
                RRule rule = new RRule(recur);
                event.getProperties().add(rule);

                // Add a 15-minute reminder
//                VAlarm reminder = new VAlarm(java.util.Calendar.MINUTE, -15);
//                reminder.getProperties().add(new Description("Reminder for " + name));
//                event.getAlarms().add(reminder);

                calendar.getComponents().add(event);
            }

            // Save the calendar to file
            FileOutputStream fileOutputStream = new FileOutputStream("jungleePoker.ics");
            CalendarOutputter calendarOutputter = new CalendarOutputter();
            calendarOutputter.output(calendar, fileOutputStream);
        } catch (IOException e) {
            System.out.println("Error reading or writing file: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Error parsing date or time: " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("Error validating calendar: " + e.getMessage());
        }
    }
}
