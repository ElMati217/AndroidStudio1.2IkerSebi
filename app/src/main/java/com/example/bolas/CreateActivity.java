package com.example.bolas;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CreateActivity extends AppCompatActivity {

    //VARIABLES DE CONEXIÓN DE LA BASE DE DATOS
    private Connection connection;
    private final String host = "10.0.2.2";
    private final String database = "pruebas";
    private final int port = 5432;
    private final String user = "postgres";
    private final String pass = "pan";
    private String url = "jdbc:postgresql://%s:%d/%s";
    private boolean status;






    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_incidencia);
        CreateNotificationChannel();


        //DELCARACIÓN DE BOTONES Y SPINNER
        Button campo_fecha = (Button) findViewById(R.id.seleccionar_fecha);
        TextView textofecha = (TextView) findViewById(R.id.textofecha);

        final Spinner dropdown = findViewById(R.id.i_tipo);
        String[] items = new String[]{"Ordenador", "Servidor", "Portatil", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);


        Button boton_cancelar = (Button) findViewById(R.id.b_cancelar);
        boton_cancelar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startActivity(new Intent(CreateActivity.this, MainActivity.class));
            }
        });


        Button boton_crear = (Button) findViewById(R.id.b_crearincidencia);
        boton_crear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                url = String.format(url, host, port, database);
                crear_incidencia();
            }
        });


        //ONCLICKLISTENER QUE NOS PERMITE GENERAR UNA INTERFAZ PARA SELECCIONAR UNA FECHA
        campo_fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();


                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        CreateActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                textofecha.setText("Fecha: " + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

    }



    //MÉTODO PARA CREAR LA INCIDENCIA
    private void crear_incidencia()
    {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    Looper.prepare();
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(url, user, pass);
                    status = true;
                    Statement query = connection.createStatement();


                    //SE SELECCIONAN TODOS LOS CAMPOS RELLENABLES Y SE RELLENAN VARIABLES CON LOS VALORES EN STRING DE ESTOS
                    EditText i_usuario = findViewById(R.id.i_usuario);
                    EditText i_elemento = findViewById(R.id.i_elemento);
                    Spinner i_tipo = findViewById(R.id.i_tipo);
                    EditText i_ubicacion = findViewById(R.id.i_ubicacion);
                    EditText i_asunto = findViewById(R.id.i_asunto);
                    EditText i_descripcion = findViewById(R.id.i_descripcion);
                    TextView textofecha = findViewById(R.id.textofecha);

                    String t_usuario = String.valueOf(i_usuario.getText());
                    String t_elemento = String.valueOf(i_elemento.getText());
                    String t_tipo = String.valueOf(i_tipo.getSelectedItem());
                    String t_ubicacion = String.valueOf(i_ubicacion.getText());
                    String t_asunto = String.valueOf(i_asunto.getText());
                    String t_descripcion = String.valueOf(i_descripcion.getText());
                    String t_textofecha = String.valueOf(textofecha.getText());

                    System.out.println(t_usuario.length());



                    //COMPROBACIONES DE QUE LOS CAMPOS TENGAN INFORMACIÓN
                    if(t_usuario.length() == 0){

                        Toast.makeText(getApplicationContext(),"El campo de usuario no puede estar vacío.",Toast.LENGTH_SHORT).show();

                    } else if(t_elemento.length() == 0) {

                        Toast.makeText(getApplicationContext(),"El campo de elemento no puede estar vacío.",Toast.LENGTH_SHORT).show();

                    } else if(t_tipo.length() == 0) {

                        Toast.makeText(getApplicationContext(),"¿Cómo has hecho eso?",Toast.LENGTH_SHORT).show();

                    } else if(t_ubicacion.length() == 0) {

                        Toast.makeText(getApplicationContext(),"El campo de ubicación no puede estar vacío.",Toast.LENGTH_SHORT).show();

                    } else if(t_asunto.length() == 0) {

                        Toast.makeText(getApplicationContext(),"El campo de asunto no puede estar vacío.",Toast.LENGTH_SHORT).show();

                    } else if(t_descripcion.length() < 10) {

                        Toast.makeText(getApplicationContext(),"Añade una descripción más larga.",Toast.LENGTH_SHORT).show();

                    } else if(t_textofecha.equals("Fecha: Ninguna")) {

                        Toast.makeText(getApplicationContext(),"Selecciona una fecha.",Toast.LENGTH_SHORT).show();

                    }  else {

                        //COMPARAMOS LA FECHA DE HOY CON LA DE LA INCIDENCIA
                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date fecha_hoy = sdf.parse(timeStamp);
                        Date fecha_incidencia = sdf.parse(t_textofecha.replace("Fecha: ", ""));

                        //SI LA FECHA ES SUPERIOR A LA DE HOY, SE DEVUELVE UN ERROR
                        if (fecha_incidencia.after(fecha_hoy)) {

                            Toast.makeText(getApplicationContext(), "La fecha tiene que ser anterior o actual.", Toast.LENGTH_SHORT).show();

                        } else {

                            //EN CASO DE QUE TODAS LAS COMPROBACIONES TENGAN ÉXITO, INSERTAMOS LA INCIDENCIA EN LA BASE DE DATOS
                            String sql = "INSERT INTO tickets (usuario, elemento, tipo_elemento, ubicacion, asunto, cuerpo, fecha, activo) VALUES ('" + t_usuario + "', '" + t_elemento + "', '" + t_tipo + "', '" + t_ubicacion + "', '" + t_asunto + "', '" + t_descripcion + "', '" + t_textofecha.replace("Fecha: ", "") + "', 'true')";
                            System.out.println(sql);
                            query.executeUpdate(sql);

                            //RECARGAMOS EL LISTADO DE INCIDENCIAS
                            Toast.makeText(getApplicationContext(), "Se ha creado la incidencia.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateActivity.this, MainActivity.class));

                        }
                    }
                }
                catch (Exception e)
                {
                    status = false;
                    System.out.print(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try
        {
            thread.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.status = false;
        }
    }


    //SIRVE PARA CREAR EL CANAL DE NOTIFICACIONES
    private void CreateNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence nombre = "notifications";
            String descripcion = "canal de notificaciones";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notifications = new NotificationChannel("notifications",nombre,importancia);
            notifications.setDescription(descripcion);


            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notifications);
        }
    }

}
