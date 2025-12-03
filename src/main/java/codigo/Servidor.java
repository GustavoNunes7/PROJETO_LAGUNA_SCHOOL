package codigo;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class Servidor {

    private static Connection con;

    public static void main(String[] args) throws Exception {

        // Conectar ao SQLite (arquivo conteudo.db na pasta do projeto)
        con = DriverManager.getConnection("jdbc:sqlite:conteudo.db");

        // Criar tabela (se não existir)
        String sql = "CREATE TABLE IF NOT EXISTS dadosx (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tarefa TEXT," +
                "materia TEXT," +
                "data TEXT," +
                "observacao TEXT," +
                "feito TEXT" +
                ")";
        con.createStatement().execute(sql);

        // Criar servidor HTTP
        HttpServer s = HttpServer.create(new InetSocketAddress(8083), 0);

        // Rotas básicas
        s.createContext("/", Servidor::login);           // processa login
        s.createContext("/login", Servidor::login);           // processa login
        s.createContext("/professor", Servidor::professor);     // cadastro
        s.createContext("/aluno", Servidor::aluno); // lista cards
        s.createContext("/avaliar", Servidor::avaliar);       // curtir / não curtir
        s.createContext("/estilo", t -> enviarCSS(t, "estilo.css")); // CSS
        s.createContext("/acessonegado", t -> enviar(t, "acessonegado.html"));
        s.createContext("/concha2.png", t -> enviarImagem(t, "/image/concha2.png")); // IMAGEM
        s.createContext("/fundo.png", t -> enviarImagem(t, "/image/fundo.png"));
        s.start();
        System.out.println("Servidor rodando em http://localhost:8083/");
    }

    // -------------------- LOGIN --------------------

    private static void login(HttpExchange t) throws IOException {
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            enviar(t, "login.html");
            return;
        }

        String corpo = ler(t); // exemplo: tipo=produtor
        corpo = URLDecoder.decode(corpo, StandardCharsets.UTF_8);

        System.out.println(corpo);




        //-------------------------------- Cadastro dos Professores-----------------------------------



        if (corpo.contains("professor")){ //perfil
            System.out.println("PROFESSOR");
            if (corpo.contains("professor")) { // usuario
                System.out.println("professor");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Professor");
                    t.getResponseHeaders().set("Location", "/professor");
                    t.sendResponseHeaders(302, -1L);

                }
            }
        }
        if (corpo.contains("professor")){ //perfil
            System.out.println("PROFESSOR");
            if (corpo.contains("arieldias")) { // usuario
                System.out.println("arieldias");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Professor Ariel Dias");
                    t.getResponseHeaders().set("Location", "/professor");
                    t.sendResponseHeaders(302, -1L);
                }
            }
        }

        if (corpo.contains("professor")){ //perfil
            System.out.println("PROFESSOR");
            if (corpo.contains("eduardofalabella")) { // usuario
                System.out.println("eduardofalabella");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Professor Eduardo Falabella");
                    t.getResponseHeaders().set("Location", "/professor");
                    t.sendResponseHeaders(302, -1L);
                }
            }
        }

        //-------------------------------- Cadastro dos Alunos-----------------------------------//

        if (corpo.contains("aluno")){ //perfil
            System.out.println("ALUNO");
            if (corpo.contains("aluno")) { // usuario
                System.out.println("aluno");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Aluno");
                    t.getResponseHeaders().set("Location", "/aluno");
                    t.sendResponseHeaders(302, -1L);
                }
            }
        }

        if (corpo.contains("aluno")){ //perfil
            System.out.println("ALUNO");
            if (corpo.contains("carinasouza")) { // usuario
                System.out.println("carinasouza");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Aluna Carina Souza");
                    t.getResponseHeaders().set("Location", "/aluno");
                    t.sendResponseHeaders(302, -1L);
                }
            }
        }

        if (corpo.contains("aluno")){ //perfil
            System.out.println("ALUNO");
            if (corpo.contains("luizatimporini")) { // usuario
                System.out.println("luizatimporini");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Aluna Luiza Timporini");
                    t.getResponseHeaders().set("Location", "/aluno");
                    t.sendResponseHeaders(302, -1L);
                }
            }
        }

        if (corpo.contains("aluno")){ //perfil
            System.out.println("ALUNO");
            if (corpo.contains("gustavonunes")) { // usuario
                System.out.println("gustavonunes");
                if(corpo.contains("1234")){ // senha
                    System.out.println("Acesso liberado: Aluno Gustavo Nunes");
                    t.getResponseHeaders().set("Location", "/aluno");
                    t.sendResponseHeaders(302, -1L);
                }
            }
        }

        else{
            System.out.println("Acesso Negado");
            t.getResponseHeaders().set("Location", "/acessonegado");
            t.sendResponseHeaders(302, -1L);
        }
   }

    // -------------------- PROFESSOR --------------------------------------//

    private static void professor(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            enviar(t, "professor.html");
            return;
        }

        String c = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        System.out.println("dados  "+c);


        String[] partes;
        partes=c.split("&");
        String tarefa =partes[0].replace("tarefa=","") ;
        String materia = partes[1].replace("materia=","");
        String data = partes[2].replace("data=","");
        String observacao = partes[3].replace("observacao=","");
        System.out.println("aaaa:" + tarefa + materia +data +observacao);

        String tare = pega(c, "tarefa");
        String mate = pega(c, "materia");
        String date = pega(c, "data");
        String obse = pega(c, "observacao");
        System.out.println(tare + mate +  date + obse);

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO dadosx (tarefa, materia, data, observacao, feito) VALUES (?,?,?,?,?)")) {

            ps.setString(1, tare);
            ps.setString(2, mate);
            ps.setString(3, date);
            ps.setString(4, obse);
            ps.setString(5, "nenhuma"); // ainda não curtido
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirecionar(t, "/professor");
    }

    // -------------------- ALUNO (lista todos os cards) --------------------

    private static void aluno(HttpExchange t) throws IOException {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>Atividades</title>");
        html.append("<link rel=\"stylesheet\" href=\"/estilo.css\">");
        html.append("</head><body>");

        html.append("<h1>Atividades</h1>");
        html.append("<p>Cada tarefa aparece em um card separado.</p>");

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, tarefa, materia, data, observacao, feito FROM dadosx ORDER BY id DESC")) {

            boolean vazio = true;

            while (rs.next()) {
                vazio = false;

                int id = rs.getInt("id");
                String tare = rs.getString("tarefa");
                String mate = rs.getString("materia");
                String data = rs.getString("data");
                String obse = rs.getString("obsersevacao");
                String feito = rs.getString("feito");

                // Classe extra para cor do card
                String classeExtra = "";
                if ("feito".equals(feito)) {
                    classeExtra = " card-feito";
                } else if ("nao".equals(feito)) {
                    classeExtra = " card-nao-feito";
                }

                html.append("<div class=\"card").append(classeExtra).append("\">");

                html.append("<p><strong>ID:</strong> ").append(id).append("</p>");
                html.append("<p><strong>Tarefa:</strong> ").append(tare).append("</p>");
                html.append("<p><strong>Matéria:</strong> ").append(mate).append("</p>");
                html.append("<p><strong>Data:</strong> ").append(data).append("</p>");
                html.append("<p><strong>Observação:</strong> ").append(obse).append("</p>");
                html.append("<p><strong>Status:</strong> ").append(feito).append("</p>");

                // Botão FEITO
                html.append("<form method=\"POST\" action=\"/avaliar\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"acao\" value=\"feito\">");
                html.append("<button type=\"submit\">Feito</button>");
                html.append("</form>");

                // Botão NÃO FEITO
                html.append("<form method=\"POST\" action=\"/avaliar\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"acao\" value=\"nao\">");
                html.append("<button type=\"submit\">Não Feito</button>");
                html.append("</form>");


                html.append("</div>");
            }

            if (vazio) {
                html.append("<p>Nenhuma atividade cadastrada ainda.</p>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            html.append("<p>Erro ao carregar atividades.</p>");
        }

        html.append("</body></html>");

        // Enviar HTML gerado
        byte[] b = html.toString().getBytes(StandardCharsets.UTF_8);
        System.out.println(b);
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    // -------------------- REALIZAR ATIVIDADE --------------------

    private static void avaliar(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/aluno");
            return;
        }

        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        String acao = pega(corpo, "acao"); // "curtir" ou "nao"
        String idStr = pega(corpo, "id");

        try {
            int id = Integer.parseInt(idStr);
            System.out.println(idStr);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE dadosx SET feito = ? WHERE id = ?")) {
                ps.setString(1, acao);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        redirecionar(t, "/aluno");
    }



    // -------------------- ENVIAR IMAGEM --------------------

    private static void enviarImagem(HttpExchange t, String arquivo) throws IOException {
        File f = new File("src/main/java/codigo" + arquivo);

        byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "image/png");
        t.sendResponseHeaders(200, bytes.length);
        t.getResponseBody().write(bytes);
        t.close();
    }

    // -------------------- Funções auxiliares --------------------

    private static String pega(String corpo, String campo) {
        // corpo no formato: campo1=valor1&campo2=valor2...
        for (String s : corpo.split("&")) {
            String[] p = s.split("=");
            if (p.length == 2 && p[0].equals(campo)) return p[1];
        }
        return "";
    }

    private static String ler(HttpExchange t) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8)
        );
        String linha = br.readLine();
        return (linha == null) ? "" : linha;
    }

    private static void enviar(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/codigo/" + arq);
        System.out.println(f);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void enviarCSS(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/codigo/" + arq);
        System.out.println("CSS  "+f);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/css; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void redirecionar(HttpExchange t, String rota) throws IOException {
        t.getResponseHeaders().add("Location", rota);
        t.sendResponseHeaders(302, -1);
        t.close();
    }
}
