/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.imc.rest;

import com.imc.config.ConexionDB;
import com.imc.model.CalculoIMC;
import com.imc.model.Usuario;
import java.sql.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("imc")
public class IMCService {

    @Context
    private HttpServletRequest request;

    @POST
    @Path("registrar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(
            @FormParam("nombre") String nombre, 
            @FormParam("edad") int edad,
            @FormParam("sexo") String sexo, 
            @FormParam("estatura") double estatura,
            @FormParam("username") String username, 
            @FormParam("password") String password) {

        String errorValidacion = Usuario.validarDatos(edad, estatura);
        if (errorValidacion != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + errorValidacion + "\"}").build();
        }

        String sql = "INSERT INTO usuarios (nombre_completo, edad, sexo, estatura, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, edad);
            ps.setString(3, sexo);
            ps.setDouble(4, estatura);
            ps.setString(5, username);
            ps.setString(6, password); 
            ps.executeUpdate();
            return Response.ok("{\"mensaje\":\"Usuario registrado con éxito.\"}").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El nombre de usuario ya se encuentra registrado.\"}").build();
        }
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    
                    HttpSession session = request.getSession(true);
                    session.setAttribute("user_id", rs.getInt("id"));
                    session.setAttribute("nombre", rs.getString("nombre_completo"));
                    session.setAttribute("estatura", rs.getDouble("estatura"));

                    return Response.ok("{\"mensaje\":\"Login exitoso\"}").build();
                }
            }
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Usuario o contraseña incorrectos.\"}").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error de conexión en el servidor.\"}").build();
        }
    }

    @POST
    @Path("calcular")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response calcularYGuardar(@FormParam("peso") double peso) {
        HttpSession session = request.getSession(false);
        
        
        if (session == null || session.getAttribute("user_id") == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Acceso denegado. Debe iniciar sesión para calcular el IMC.\"}").build();
        }

        
        if (peso <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"La masa corporal debe ser una cantidad mayor a 0 kg.\"}").build();
        }

        int usuarioId = (int) session.getAttribute("user_id");
        double estatura = (double) session.getAttribute("estatura");
        double imcResultado = CalculoIMC.calcular(peso, estatura);

        String sql = "INSERT INTO historico_imc (usuario_id, peso, imc) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setDouble(2, peso);
            ps.setDouble(3, imcResultado);
            ps.executeUpdate();
            return Response.ok("{\"imc\":" + imcResultado + "}").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al registrar el cálculo en la base de datos.\"}").build();
        }
    }

    @GET
    @Path("historico")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerHistorico() {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Sesión no válida.\"}").build();
        }

        int usuarioId = (int) session.getAttribute("user_id");
        String sql = "SELECT peso, imc, DATE_FORMAT(fecha, '%Y-%m-%d %H:%i') as f FROM historico_imc WHERE usuario_id = ? ORDER BY id DESC";
        
        StringBuilder json = new StringBuilder("[");
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean primero = true;
                while (rs.next()) {
                    if (!primero) json.append(",");
                    json.append("{")
                        .append("\"peso\":").append(rs.getDouble("peso")).append(",")
                        .append("\"imc\":").append(rs.getDouble("imc")).append(",")
                        .append("\"fecha\":\"").append(rs.getString("f")).append("\"")
                        .append("}");
                    primero = false;
                }
            }
            json.append("]");
            return Response.ok(json.toString()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al procesar el histórico.\"}").build();
        }
    }
}
