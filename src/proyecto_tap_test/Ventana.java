/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto_tap_test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

/**
 *
 * @author josea
 */
public class Ventana extends java.awt.Frame {

    Dimension d = new Dimension(1280, 720);
    Game game;
    private boolean corriendo = false;
    private boolean pausado = false;
    private int fps = 60;
    private int frameCount = 0;

    private Status statusDialog;

    public Ventana() {
        initComponents();
        statusDialog = new Status();
        statusDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
        game = new Game(d);
        this.setSize(d);
        this.add(this.game);
        runGameLoop();
        fileLoop();

        // Crea un evento de escuchar al teclado para posibles eventos
        this.addKeyListener(new KeyListener() {
            // Cuando la tecla Escape es tipeada entonces cierra por completo el juego
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }

            }

            // Cuando la tecla S es presionada activa la ventana de configuraciones "Status"
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    System.out.println("" + statusDialog.isVisible());
                    statusDialog.setVisible(!statusDialog.isVisible());
                    setAlwaysOnTop(!statusDialog.isVisible());
                    statusDialog.setFocusable(statusDialog.isVisible());
                    statusDialog.setAlwaysOnTop(statusDialog.isVisible());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setAlwaysOnTop(true);
        setBackground(java.awt.Color.black);
        setBounds(new java.awt.Rectangle(0, 0, 800, 450));
        setMaximumSize(new java.awt.Dimension(800, 450));
        setMinimumSize(new java.awt.Dimension(100, 100));
        setPreferredSize(new java.awt.Dimension(800, 450));
        setResizable(false);
        setSize(new java.awt.Dimension(800, 450));
        setTitle("Game");
        setType(java.awt.Window.Type.POPUP);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        if (evt.getKeyCode() == 27) {
            System.exit(0);
        } else if (evt.getKeyCode() == 10) {
            JOptionPane.showMessageDialog(this, "uwu");
        }

    }//GEN-LAST:event_formKeyPressed

    /**
     * Proporciona un hilo de ejecucion para imprimir datos importantes del juego en un archivo log.txt
     */
    public void fileLoop() {
        Thread loop = new Thread() {
            public void run() {
                while (true) {
                    try (FileWriter fw = new FileWriter("log.txt", true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw)) {
                        DateFormat date = DateFormat.getDateTimeInstance();
                        out.print("[" + date.format(new Date()) + "]");
                        Color color = game.particula.mColor;
                        out.print(" Color: " + String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
                        int[] posicion = game.particula.Posicion;
                        out.print(" Posicion: (" + posicion[0] + ", " + posicion[1] + ")");
                        out.println();
                    } catch (IOException e) {
                        //exception handling left as an exercise for the reader
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {

                    }
                }
            }
        };
        loop.start();
    }

    //Inicia un nuevo hilo y corre el game loop en él
    public void runGameLoop() {
        Thread loop = new Thread() {
            public void run() {
                corriendo = true;
                gameLoop();
            }
        };
        loop.start();
    }

    /**
     * Es el hilo de ejecucion del juego
     * Se encarga de controlar cuando actualiza y cuando debe dibujar en pantalla
     */
    private void gameLoop() {
        final double GAME_HERTZ = 30.0;
        //Calcular cuántos nanosegundos tomará cada frame para conseguir los hertz deseados
        final double TIEMPO_ENTRE_ACTUALIZACIONES = 1000000000 / GAME_HERTZ;
        //If you're worried about visual hitches more than perfect timing, set this to 1.
        final int MAX_ACTUALIZACIONES_PRE_RENDERIZAR = 5;

        //Último tiempo de actualización.
        double ultTiempoActualizacion = System.nanoTime();

        //Último tiempo de renderizado.
        double ultTiempoRenderizado = System.nanoTime();

        //Si es posible conseguir estos FPS, no volver a renderizar.
        final double TARGET_FPS = 60;
        final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

        //Manera simple de encontrar FPS.
        int lastSecondTime = (int) (ultTiempoActualizacion / 1000000000);

        while (corriendo) {
            double now = System.nanoTime();
            int contActualizaciones = 0;

            if (!pausado) {
                //Actualizar el juego las veces que sea necesario
                while (now - ultTiempoActualizacion > TIEMPO_ENTRE_ACTUALIZACIONES && contActualizaciones < MAX_ACTUALIZACIONES_PRE_RENDERIZAR) {
                    actualizarJuego();
                    ultTiempoActualizacion += TIEMPO_ENTRE_ACTUALIZACIONES;
                    contActualizaciones++;
                }

                if (now - ultTiempoActualizacion > TIEMPO_ENTRE_ACTUALIZACIONES) {
                    ultTiempoActualizacion = now - TIEMPO_ENTRE_ACTUALIZACIONES;
                }

                dibujarJuego();
                ultTiempoRenderizado = now;

                //Actualizar los frames obtenidos
                int thisSecond = (int) (ultTiempoActualizacion / 1000000000);
                if (thisSecond > lastSecondTime) {
                    fps = frameCount;
                    frameCount = 0;
                    lastSecondTime = thisSecond;
                }

                while (now - ultTiempoRenderizado < TARGET_TIME_BETWEEN_RENDERS && now - ultTiempoActualizacion < TIEMPO_ENTRE_ACTUALIZACIONES) {
                    try {

                        Thread.sleep(1);
                    } catch (Exception e) {
                    }
                    now = System.nanoTime();
                }
            }
        }
    }

    private void actualizarJuego() {
        game.actualizar();
        game.particula.setColorRayos(statusDialog.getColor());
        statusDialog.setFps(fps);
        if (statusDialog.hayCambios) {
            statusDialog.hayCambios = false;
            loadChanges();
        }
    }

    /**
     * Cuando detecta algún cambio en la ventana de configuracion entonces esto 
     * se encarga de hacer efectivos esos cambios
     */
    private void loadChanges() {
        try {
            if (statusDialog.getDim() != null) {
                this.d = statusDialog.getDim();
                this.setSize(d);
                game.updateDim(d);
            }
            game.particula.setRayos(statusDialog.getCantRayos());
        } catch (Exception e) {

        }
    }

    private void dibujarJuego() {
        game.repaint();
        frameCount++;
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ventana().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
