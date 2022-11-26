package com.example.bolas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    String usuario, contra;

    //CREAMOS LAS VARIABLES DE LA BASE DE DATOS
    private Connection connection;
    private final String host = "10.0.2.2";
    private final String database = "pruebas";
    private final int port = 5432;
    private final String user = "postgres";
    private final String pass = "pan";
    private String url = "jdbc:postgresql://%s:%d/%s";
    private boolean status;
    private boolean usuario_correcto;
    @SuppressLint("NewApi")
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    //CREAMOS LA VARIABLE QUE ALMACENARÁ NUESTRA LISTVIEW
    private ListView lv;



    public void onCreate(android.os.Bundle savedInstanceState) {
        //CARGAMOS NUESTRO LAYOUT
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INICIALIZAMOS EL TEXTO SI NO HAY INCIDENCIAS Y EL BOTÓN DE REFRESCAR
        Button boton_refrescar = findViewById(R.id.b_refrescar);
        TextView texto_sin = findViewById(R.id.sin_incidencias);


        //ASIGNAMOS UN ONLICK LISTENER AL BOTÓN DE ACTUALIZAR
        boton_refrescar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        //INICIALIZAMOS EL BOTÓN DE CREAR Y LE ASIGNAMOS UN ONCLICKLISTENER
        Button boton_crear = findViewById(R.id.b_crear);

        boton_crear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, CreateActivity.class));
            }
        });

        //CREAMOS LA CONEXIÓN QUE POBLARÁ NUESTRA LISTVIEW Y LE PASAMOS EL CONTEXTO NECESARIO
        prueba_conexion(texto_sin);

    }

    private void prueba_conexion(TextView texto_sin)
    {
        //CREAMOS LA URL Y LA FORMATAMOS PARA QUE ESTA SEA USABLE
        this.url = String.format(this.url, this.host, this.port, this.database);
        connect(texto_sin);
        //this.disconnect();

    }

    private void connect(TextView texto_sin)
    {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    //CREAMOS E INICIAMOS LA CONEXIÓN
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(url, user, pass);
                    status = true;
                    Statement query = connection.createStatement();

                    //CONTAMOS CUANTAS INCIDENCIAS HAY
                    String sql = "SELECT COUNT(*) FROM tickets WHERE activo = 'true'";
                    ResultSet rs = query.executeQuery(sql);

                    int longitud = 0;

                    while (rs.next()){
                        longitud = rs.getInt(1);
                    }


                    //EN CASO DE HABER 1 O MÁS, EL TEXTO DE QUE NO HAY INCIDENCIAS NO SE MUESTRA
                    if(longitud != 0){
                        texto_sin.setText("");
                    }

                    //CREAMOS ARRAYS QUE ALMACENARÁN LA INFORMACIÓN PARA LA LISTVIEW
                    String[] titulos =  new String[longitud];
                    String[] cuerpos =  new String[longitud];
                    String[] usuarios =  new String[longitud];
                    String[] idS =  new String[longitud];
                    String[] tipos_elemento =  new String[longitud];
                    String[] elementos =  new String[longitud];
                    String[] fechas =  new String[longitud];
                    String[] ubicaciones = new String[longitud];
                    int[] imagenes = new int[longitud];
                    Button[] botones =  new Button[longitud];

                    //SELECCIONAMOS TODA LA INFORMACIÓN DE LA TABLA DE TICKETS
                    sql = "SELECT * FROM tickets WHERE activo = 'true' ORDER BY id ASC";
                    rs = query.executeQuery(sql);

                    lv = (ListView) findViewById(R.id.xd);

                    int contador = 0;

                    //EN CASO DE ENCONTRAR REGISTROS, INSERTAMOS EN LOS ARRAYS LA INFORMACIÓN
                    while (rs.next()) {
                        titulos[contador] = rs.getString("asunto");
                        cuerpos[contador] = rs.getString("cuerpo");
                        usuarios[contador] = rs.getString("usuario");
                        idS[contador] = String.valueOf(rs.getInt("id"));
                        tipos_elemento[contador] = rs.getString("tipo_elemento");
                        elementos[contador] = rs.getString("elemento");
                        fechas[contador] = rs.getString("fecha");
                        ubicaciones[contador] = rs.getString("ubicacion");
                        if(rs.getString("tipo_elemento").equals("Servidor")){
                            imagenes[contador] = R.drawable.servidor;
                        } else if(rs.getString("tipo_elemento").equals("Ordenador")) {
                            imagenes[contador] = R.drawable.dejadelpc;
                        } else if(rs.getString("tipo_elemento").equals("Portatil")) {
                            imagenes[contador] = R.drawable.portatil;
                        } else {
                            imagenes[contador] = R.drawable.quecojoneseseso;
                        }
                        contador++;
                    }

                    //LE INTRODUCIMOS AL LISTVIEW LOS REGISTROS PARA QUE LOS INTERPRETE
                    CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), titulos, cuerpos, usuarios, idS, tipos_elemento, elementos, fechas, ubicaciones, imagenes, botones);
                    lv.setAdapter(customAdapter);

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