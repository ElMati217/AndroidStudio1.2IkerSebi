package com.example.bolas;

import static android.content.Intent.getIntent;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CustomAdapter extends BaseAdapter {

    //CREAMOS LAS VARIABLES QUE ALMACENARÁN LA INFORMACIÓN DE LA LISTVIEW
    Context context;
    String titulos[];
    String cuerpos[];
    String usuarios[];
    String idS[];
    String tipos_elemento[];
    String elementos[];
    String fechas[];
    String ubicaciones[];
    int imagenes[];
    Button botones[];
    LayoutInflater layoutInflater;
    private Context contexto;

    //VARIABLES PARA CONECTARSE A LA BASE DE DATOS
    private Connection connection;
    private final String host = "10.0.2.2";
    private final String database = "pruebas";
    private final int port = 5432;
    private final String user = "postgres";
    private final String pass = "pan";
    private String url = "jdbc:postgresql://%s:%d/%s";
    private boolean status;

    //CONSTRUCTOR QUE INSERTARÁ EN LAS VARIABLES LA INFORMACIÓN QUE LE PASAMOS DESDE LA SELECT DEL MAIN
    public CustomAdapter(Context ctx, String[] titulos, String[] asuntos, String[] usuarios, String[] idS, String[] tipos_elemento, String[] elementos, String[] fechas, String[] ubicaciones, int[] imagenes, Button[] botones){
        this.url = String.format(this.url, this.host, this.port, this.database);
        this.context = ctx;
        this.titulos = titulos;
        this.cuerpos = asuntos;
        this.usuarios = usuarios;
        this.idS = idS;
        this.tipos_elemento = tipos_elemento;
        this.elementos = elementos;
        this.fechas = fechas;
        this.ubicaciones = ubicaciones;
        this.imagenes = imagenes;
        this.botones = botones;
        layoutInflater = LayoutInflater.from(ctx);

    }


    @Override
    public int getCount() {
        return titulos.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.listview_incidencias, null);

        //CREAMOS LAS VARIABLES DE LOS CAMPOS DE TEXTO, IMAGENES, ETC.
        ImageView imageView = (ImageView) convertView.findViewById(R.id.icono);
        Button boton = (Button) convertView.findViewById(R.id.boton_cerrar);
        TextView textView = (TextView) convertView.findViewById(R.id.titulo);
        TextView textView1 = (TextView) convertView.findViewById(R.id.cuerpo);
        TextView textView2 = (TextView) convertView.findViewById(R.id.usuario_creador);
        TextView textView3 = (TextView) convertView.findViewById(R.id.id_incidencia);
        TextView textView4 = (TextView) convertView.findViewById(R.id.tipo_elemento);
        TextView textView5 = (TextView) convertView.findViewById(R.id.elemento);
        TextView textView6 = (TextView) convertView.findViewById(R.id.fecha_creacion);
        TextView textView7 = (TextView) convertView.findViewById(R.id.ubicacion);

        imageView.setImageResource(imagenes[position]);

        //AÑADIMOS UN ACTIONLISTENER QUE SIRVA PARA CERRAR LA INCIDENCIA Y ADEMÁS REFRESCA LA LISTA
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect(idS[position], titulos[position]);
                Toast.makeText(context,"Se ha cerrado la incidencia.",Toast.LENGTH_SHORT).show();
                Intent myIntent = new Intent(context, MainActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(myIntent);
            }
        });


        //INSERTAMOS LA INFORMACIÓN RECIBIDA EN LOS CAMPOS
        textView.setText("Título: " + titulos[position]);
        textView1.setText("Descripción: " + cuerpos[position]);
        textView2.setText("Usuario: " + usuarios[position]);
        textView3.setText("ID: " + idS[position]);
        textView4.setText("Tipo: " + tipos_elemento[position]);
        textView5.setText("Elemento: " + elementos[position]);
        textView6.setText("Fecha: " + fechas[position]);
        textView7.setText("Lugar: " + ubicaciones[position]);
        return convertView;
    }

    //MÉTODO QUE SIRVE PARA CREAR UN CANAL DE NOTIFICACIONES
    private void CreateNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence nombre = "notifications";
            String descripcion = "canal de notificaciones";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notifications = new NotificationChannel("notifications",nombre,importancia);
            notifications.setDescription(descripcion);


            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notifications);
        }
    }

    private void connect(String id, String titulo)
    {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(url, user, pass);
                    status = true;
                    Statement query = connection.createStatement();

                    //UPDATE PARA CAMBIAR EL ESTADO DE LA INCIDENCIA A CERRADO, PILLAMOS LA ID DE UN PARÁMETRO QUE PASAMOS A TRAVÉS DE LA FUNCIÓN
                    String sql = "UPDATE tickets SET activo = 'false' WHERE id = "+id;
                    System.out.println(sql);
                    query.executeUpdate(sql);


                    //CREAMOS UNA NOTIFICACIÓN DE QUE SE HA CERRADO LA INCIDENCIA Y LA MANDAMOS
                    CreateNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifications")
                            .setSmallIcon(R.drawable.quebien)
                            .setContentTitle("Incidencia cerrada.")
                            .setContentText("La incidencia " + titulo + " con ID: " + id + " ha sido cerrada.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(100, builder.build());



                    System.out.println("connected:" + status);
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
}


