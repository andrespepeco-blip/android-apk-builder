package com.tuempresa.pruebaextrema;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private boolean isRunning = false;
    private Handler handler = new Handler();
    private Button btnToggle;
    private TextView tvEstado, tvCiclos, tvCPU, tvArchivos, tvWorkers;
    private int contadorCiclos = 0;
    private int contadorArchivos = 0;
    private Thread[] hilos = new Thread[20];
    private int numHilos = 0;
    private boolean hilosActivos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = findViewById(R.id.btnToggle);
        tvEstado = findViewById(R.id.tvEstado);
        tvCiclos = findViewById(R.id.tvCiclos);
        tvCPU = findViewById(R.id.tvCPU);
        tvArchivos = findViewById(R.id.tvArchivos);
        tvWorkers = findViewById(R.id.tvWorkers);

        btnToggle.setOnClickListener(v -> {
            if (isRunning) {
                detenerPrueba();
            } else {
                iniciarPrueba();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "test_channel",
                "Pruebas de seguridad",
                NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void iniciarPrueba() {
        if (isRunning) return;

        isRunning = true;
        hilosActivos = true;
        contadorCiclos = 0;
        contadorArchivos = 0;
        numHilos = Math.min(Runtime.getRuntime().availableProcessors(), 8);

        tvEstado.setText("🔴 PRUEBA ACTIVA - CONGELANDO");
        tvEstado.setBackgroundColor(0x44FF0000);
        btnToggle.setText("⏹ DETENER PRUEBA");
        btnToggle.setBackgroundColor(0xFFFF0000);

        iniciarHilosCarga();

        new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                runOnUiThread(() -> {
                    tvCiclos.setText(String.valueOf(contadorCiclos));
                    tvArchivos.setText(String.valueOf(contadorArchivos));
                    tvWorkers.setText(numHilos + " hilos");
                    int cpu = Math.min(100, 30 + new Random().nextInt(70));
                    tvCPU.setText(cpu + "%");
                });
            }
        }).start();
    }

    private void iniciarHilosCarga() {
        for (int i = 0; i < numHilos; i++) {
            final int hiloId = i;
            hilos[i] = new Thread(() -> {
                double x = 0;
                while (hilosActivos) {
                    for (int j = 0; j < 50000000; j++) {
                        x += Math.sqrt(j) * Math.sin(j) * Math.cos(j / 2) * Math.tan(j % 100);
                        x += Math.pow(j % 200, 1.5) * Math.PI;
                        x += Math.atan(j % 1000) * Math.acos(Math.sin(j / 100));
                        x += Math.log(j + 1) * Math.E * Math.sin(j / 10);

                        if (j % 100 == 0) {
                            double[] arr = new double[100];
                            for (int k = 0; k < arr.length; k++) {
                                arr[k] = Math.sqrt(k * j) % 10000;
                                x += arr[k] * Math.sin(k);
                            }
                        }

                        if (j % 500 == 0) {
                            StringBuilder str = new StringBuilder();
                            for (int k = 0; k < 100; k++) {
                                str.append((char)(65 + (k % 26)));
                            }
                            x += str.length();
                        }

                        if (j % 1000 == 0) {
                            contadorArchivos++;
                            contadorCiclos++;
                            if (contadorArchivos % 100 == 0) {
                                enviarNotificacion("🔴 " + contadorArchivos + " archivos cifrados");
                            }
                        }
                    }

                    if (hiloId == 0) {
                        String[] mensajes = {
                            "СИСТЕМА УНИЧТОЖЕНА!",
                            "ДОСТУП ЗАБЛОКИРОВАН",
                            "ФАЙЛЫ ЗАШИФРОВАНЫ",
                            "СЕТЬ КОМПРОМЕТИРОВАНА"
                        };
                        Random r = new Random();
                        enviarNotificacion("☠️ " + mensajes[r.nextInt(mensajes.length)]);
                    }
                }
            });
            hilos[i].start();
        }
    }

    private void detenerPrueba() {
        isRunning = false;
        hilosActivos = false;

        for (int i = 0; i < hilos.length; i++) {
            if (hilos[i] != null && hilos[i].isAlive()) {
                try {
                    hilos[i].interrupt();
                    hilos[i].join(100);
                } catch (InterruptedException e) {}
                hilos[i] = null;
            }
        }

        tvEstado.setText("🟢 SISTEMA EN ESPERA");
        tvEstado.setBackgroundColor(0x4411FF11);
        btnToggle.setText("▶ INICIAR PRUEBA");
        btnToggle.setBackgroundColor(0xFF00FF88);
        tvCPU.setText("0%");
        tvWorkers.setText("0 hilos");
        enviarNotificacion("⏹️ Prueba detenida. " + contadorArchivos + " archivos procesados.");
    }

    private void enviarNotificacion(String mensaje) {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) return;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "test_channel")
                .setContentTitle("🔒 Prueba de seguridad")
                .setContentText(mensaje)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

            manager.notify(new Random().nextInt(10000), builder.build());
        } catch (Exception e) {}
    }

    @Override
    protected void onDestroy() {
        detenerPrueba();
        super.onDestroy();
    }
}