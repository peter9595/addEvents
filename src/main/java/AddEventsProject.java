import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddEventsProject {
    private static Date firstDateAsDate;
    private static Scanner scanner = new Scanner(System.in);//Создаём объект Scanner и помещаем в его конструктор системный ввод
    private static Calendar firstDateCalendar = Calendar.getInstance();//Получаем себе объект Calendar
    private static Calendar bufferedCallendar = Calendar.getInstance();//Получаем себе объект Calendar
    private static String nextRepetitionDate;
    private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");//К переменной formatter привязываем новый объект типа SimpleDateFormat
    // с шаблоном dd/MM/yyyy
    static ArrayList<String> repetiionDayList = new ArrayList<String>();

    private static final String APPLICATION_NAME = "Google Calendar API Adding Events";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = AddEventsProject.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) throws ParseException, GeneralSecurityException, IOException {

        //Задача 1. Получить от пользователя дату освоения(выучивания)
        System.out.println("Введите дату первого изучения по шаблону : dd/MM/yyyy. БЕЗ ПРОБЕЛОВ");//Проводим инструктаж пользователю о том как
        // пользоваться скриптом
        String dateOfFirstLearning = scanner.nextLine(); //Привязываем к строковой переменной dateOfFirstLearning считнную строку от сканера
        System.out.println("Получена дата первого изучения");//отчитываемся пользователю об успешном чтении из системного ввода

        //Задача 2. На основании даты сгенерить даты повторения
        firstDateAsDate = formatter.parse(dateOfFirstLearning);//Парсируем строковую дату введённую пользователем в формат(шаблон) понятный типу Date. Делаем это
        // при помощи метода parse() класса SimpleDateFormat. Таким образом класс Date сможет применять свои методы к переменной firstDateAsDate.
        firstDateCalendar.setTime(firstDateAsDate);
        bufferedCallendar.setTime(firstDateAsDate);
        int n = 10, t1 = 1, t2 = 1;
        for (int i = 1; i <= n; ++i)
        {
            int sum = t1 + t2;
            t1 = t2;
            t2 = sum;
            bufferedCallendar.add(Calendar.DAY_OF_MONTH, t1);
            nextRepetitionDate = formatter.format(bufferedCallendar.getTime());
            repetiionDayList.add(nextRepetitionDate);
        }

        //Задача 3. На основе данных из листа и переменной с датой заучивания создать события AllDayEvents в гугл календаре.

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        for (int i = 0; i < repetiionDayList.size();i++)
        {
            String repetitionDate = repetiionDayList.get(i);
            String[] strings = repetitionDate.split("/");
            StringBuilder finalDate = new StringBuilder();
            finalDate.append(strings[2] + "-");
            finalDate.append(strings[1]+ "-");
            finalDate.append(strings[0]);
            System.out.println(finalDate);

            Event event = new Event()
                    .setSummary( "Повотрить слова за: " + formatter.format(firstDateCalendar.getTime()));
            //.setLocation("800 Howard St., San Francisco, CA 94103")
            //.setDescription("A chance to hear more about Google's developer products.");

            DateTime startDateTime = new DateTime(String.valueOf(finalDate));//T09:00:00-07:00
            EventDateTime start = new EventDateTime()
                    .setDate(startDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setStart(start);

            DateTime endDateTime = new DateTime(String.valueOf(finalDate));//T17:00:00-07:00
            EventDateTime end = new EventDateTime()
                    .setDate(endDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setEnd(end);
            EventReminder[] reminderOverrides = new EventReminder[] {
                    //new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(5 * 60),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            String calendarId = "primary";
            event = service.events().insert(calendarId, event).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());

        }

        for (int i = 0; i < repetiionDayList.size();i++)
        {
            System.out.println(repetiionDayList.get(i));
        }
    }
}
