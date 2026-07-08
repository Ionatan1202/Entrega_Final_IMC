/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.imc.model;

public class CalculoIMC {
    private double peso;
    private double imc;
    private String fecha;

    
    public static double calcular(double peso, double estatura) {
        double resultado = peso / (estatura * estatura);
        return Math.round(resultado * 100.0) / 100.0; 
    }

    
    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }
    public double getImc() { return imc; }
    public void setImc(double imc) { this.imc = imc; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
