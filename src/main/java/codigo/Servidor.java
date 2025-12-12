package codigo;

import com.sun.net.httpserver.HttpServer; // Biblioteca para criar servidor HTTP embutido
import com.sun.net.httpserver.HttpExchange; // Representa uma requisição/response HTTP
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder; // Decodifica dados de formulário
import java.nio.charset.StandardCharsets;
import java.sql.*; // JDBC para banco SQLite

public class Servidor {

    private static Connection con; // Conexão global com o banco de dados

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

        // Criar servidor HTTP na porta 8083
        HttpServer s = HttpServer.create(new InetSocketAddress(8083), 0);

        // Definição das rotas
        s.createContext("/", Servidor::login);            // Tela e processamento do login
        s.createContext("/login", Servidor::login);       // Mesma função do login
        s.createContext("/professor", Servidor::professor);   // Cadastrar atividades
        s.createContext("/aluno", Servidor::aluno);       // Listar cards
        s.createContext("/avaliar", Servidor::avaliar);   // Marcar como feito / não feito
        s.createContext("/estilo", t -> enviarCSS(t, "estilo.css")); // Enviar CSS
        s.createContext("/acessonegado", t -> enviar(t, "acessonegado.html")); // Página de acesso negado

        // Envio de imagens estáticas
        s.createContext("/concha2.png", t -> enviarImagem(t, "/image/concha2.png"));
        s.createContext("/fundo.png", t -> enviarImagem(t, "/image/fundo.png"));

        s.start();
        System.out.println("Servidor rodando em http://localhost:8083/");
    }

    // -------------------- LOGIN --------------------
    private static void login (HttpExchange t) throws IOException {
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            enviar(t, "login.html"); // Exibe página de login
            return;
        }

        String corpo = ler(t); // Lê corpo do formulário
        corpo = URLDecoder.decode(corpo, StandardCharsets.UTF_8);

        // Divide os campos enviados pelo formulário
        String[] partes = corpo.split("&");
        String usuario = partes[0].replace("usuario=", "");
        String senha = partes[1].replace("senha=", "");
        String perfil = partes[2].replace("perfil=", "");

        // ---------------- Validação de LOGIN --------------------

        // ----------- Professores --------------
        if (perfil.contains("professor")) {

            // Logins permitidos
            if (usuario.equals("professor") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Professor");
                t.getResponseHeaders().set("Location", "/professor");
                t.sendResponseHeaders(302, -1); // Redireciona
            }
            else if (usuario.equals("arieldias") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Professor Ariel Dias");
                t.getResponseHeaders().set("Location", "/professor");
                t.sendResponseHeaders(302, -1);
            }
            else if (usuario.equals("eduardofalabella") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Professor Eduardo Falabella");
                t.getResponseHeaders().set("Location", "/professor");
                t.sendResponseHeaders(302, -1);
            }
            else {
                System.out.println("Acesso negado");
                t.getResponseHeaders().set("Location", "/acessonegado");
                t.sendResponseHeaders(302, -1);
            }
        }

        // ----------- Alunos --------------
        else if (perfil.contains("aluno")) {

            if (usuario.equals("aluno") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Aluno");
                t.getResponseHeaders().set("Location", "/aluno");
                t.sendResponseHeaders(302, -1);

            } else if (usuario.equals("gustavonunes") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Aluno Gustavo Nunes");
                t.getResponseHeaders().set("Location", "/aluno");
                t.sendResponseHeaders(302, -1);

            } else if (usuario.equals("carinasouza") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Aluna Carina Souza");
                t.getResponseHeaders().set("Location", "/aluno");
                t.sendResponseHeaders(302, -1);

            } else if (usuario.equals("luizatimporini") && senha.equals("1234")) {
                System.out.println("Acesso autorizado: Aluna Luiza Timporini");
                t.getResponseHeaders().set("Location", "/aluno");
                t.sendResponseHeaders(302, -1);

            } else {
                System.out.println("Acesso negado");
                t.getResponseHeaders().set("Location", "/acessonegado");
                t.sendResponseHeaders(302, -1);
            }
        }
        else {
            System.out.println("Acesso negado");
            t.getResponseHeaders().set("Location", "/acessonegado");
            t.sendResponseHeaders(302, -1);
        }
    }

    // -------------------- PROFESSOR --------------------
    private static void professor(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            enviar(t, "professor.html"); // Exibe formulário para cadastrar tarefa
            return;
        }

        String c = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);

        // Divide os campos do formulário
        String[] partes;
        partes = c.split("&");
        String tarefa = partes[0].replace("tarefa=", "");
        String materia = partes[1].replace("materia=", "");
        String data = partes[2].replace("data=", "");
        String observacao = partes[3].replace("observacao=", "");

        // Usa método auxiliar para pegar cada campo
        String tare = pega(c, "tarefa");
        String mate = pega(c, "materia");
        String date = pega(c, "data");
        String obse = pega(c, "observacao");

        // Insere no banco de dados SQLite
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO dadosx (tarefa, materia, data, observacao, feito) VALUES (?,?,?,?,?)")) {

            ps.setString(1, tare);
            ps.setString(2, mate);
            ps.setString(3, date);
            ps.setString(4, obse);
            ps.setString(5, "nenhuma"); // Status inicial
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirecionar(t, "/professor"); // Recarrega página
    }

    // -------------------- ALUNO --------------------
    private static void aluno(HttpExchange t) throws IOException {
        StringBuilder html = new StringBuilder();

        // Monta página HTML manualmente
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>Atividades</title>");
        html.append("<link rel=\"stylesheet\" href=\"/estilo.css\">");
        html.append("</head><body>");

        html.append("<h1>Atividades</h1>");
        html.append("<p>Cada tarefa aparece em um card separado.</p>");

        // Busca tarefas no banco
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, tarefa, materia, data, observacao, feito FROM dadosx ORDER BY id DESC")) {

            boolean vazio = true;

            while (rs.next()) {
                vazio = false;

                int id = rs.getInt("id");
                String tare = rs.getString("tarefa");
                String mate = rs.getString("materia");
                String data = rs.getString("data");
                String obse = rs.getString("observacao");
                String feito = rs.getString("feito");

                // Aplica estilo dependendo do status
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
                html.append("<input type=\"hidden\" name=\"acao\" value=\"Feito\">");
                html.append("<button type=\"submit\">Feito</button>");
                html.append("</form>");

                // Botão NÃO FEITO
                html.append("<form method=\"POST\" action=\"/avaliar\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"acao\" value=\"Não Feito\">");
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

        // Envia a resposta HTML
        byte[] b = html.toString().getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    // -------------------- AVALIAÇÃO DA TAREFA --------------------
    private static void avaliar(HttpExchange t) throws IOException {

        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/aluno");
            return;
        }

        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        String acao = pega(corpo, "acao"); // Feito ou Não Feito
        String idStr = pega(corpo, "id");

        try {
            int id = Integer.parseInt(idStr);

            // Atualiza banco
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
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void enviarCSS(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/codigo/" + arq);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/css; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void redirecionar(HttpExchange t, String rota) throws IOException {
        t.getResponseHeaders().add("Location", rota);
        t.sendResponseHeaders(302, -1); // Código de redirecionamento
        t.close();
    }
}