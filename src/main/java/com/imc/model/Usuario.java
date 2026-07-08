/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.imc.model;

public class Usuario {
    private int id;
    private String nombreCompleto;
    private int edad;
    private String sexo;
    private double estatura;
    private String username;
    private String password;

    
    public static String validarDatos(int edad, double estatura) {
        if (edad < 15) {
            return "La edad mínima permitida es de 15 años.";
        }
        if (estatura < 1.0 || estatura > 2.5) {
            return "La estatura debe estar entre 1.0m y 2.5m.";
        }
        return null; 
    }

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombre) { this.nombreCompleto = nombre; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public double getEstatura() { return estatura; }
    public void setEstatura(double estatura) { this.estatura = estatura; }
    public String getUsername() { return username; }
    public void setUsername(String user) { this.username = user; }
    public String getPassword() { return password; }
    public void setPassword(String pass) { this.password = pass; }
}
