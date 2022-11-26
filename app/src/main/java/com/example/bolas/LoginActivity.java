package com.example.bolas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity  {

    String usuario, contra;

    //INFORMACIÓN DE LA CONEXIÓN CON LA BASE DE DATOS
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




    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_login);

        //DELCARAMOS TOASTS Y LOS CAMPOS DE TEXTO DEL INICIO DE SESIÓN
        EditText t_usuario = findViewById(R.id.editTextTextPersonName);
        EditText t_contra = findViewById(R.id.editTextTextPassword);
        Toast toast_bien = Toast.makeText(getApplicationContext(), "¡Sesión iniciada!", Toast.LENGTH_SHORT);
        Toast toast_mal = Toast.makeText(getApplicationContext(), "Usuario incorrecto.", Toast.LENGTH_SHORT);


        //DELCARAMOS EL BOTÓN CON UN ACTIONLISTENER EL CUAL, EN CASO DE QUE LA CONEXIÓN SEA CORRECTA Y HAYA UN USUARIO VÁLIDO, REDIRIGIRÁ AL MAIN
        Button b_iniciar_sesion = findViewById(R.id.button);
        b_iniciar_sesion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                usuario = String.valueOf(t_usuario.getText());
                contra = String.valueOf(t_contra.getText());
                prueba_conexion();
                if(usuario_correcto == true){

                    toast_bien.show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    toast_mal.show();
                }

            }
        });
    }

    //FORMATAMOS URL Y CONECTAMOS
    private void prueba_conexion()
    {
        this.url = String.format(this.url, this.host, this.port, this.database);
        connect();
        //this.disconnect();

    }

    private void connect()
    {
        EditText t_usuario = findViewById(R.id.editTextTextPersonName);
        EditText t_contra = findViewById(R.id.editTextTextPassword);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(url, user, pass);
                    status = true;
                    Statement query = connection.createStatement();


                    //ENCRIPTAMOS LA CONTRASEÑA CON MD5
                    byte[] bytes_contra = digest(t_contra.getText().toString().getBytes(UTF_8));
                    String contra_encriptada = bytesToHex(bytes_contra);


                    //REALIZAMOS LA CONEXIÓN, SI DEVUELVE UN VALOR, EL LOGIN ES CORRECTO Y SE CARGA OTRA ACTIVIDAD, EN CASO CONTRARIO, SE MOSTRARÁ UN TOAST DE FALLO Y NO INICIARÁ SESIÓN
                    String sql = "SELECT prueba1 FROM usuarios WHERE usuario = '" + t_usuario.getText().toString() + "' AND contra = '" + contra_encriptada + "'";
                    System.out.println(sql);
                    ResultSet rs = query.executeQuery(sql);

                    if (rs.next()) {
                        usuario_correcto = true;
                    } else {
                        usuario_correcto = false;
                    }

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


    //FUNCIONES PARA CONVERTIR A MD5
    private static byte[] digest(byte[] input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        byte[] result = md.digest(input);
        return result;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}