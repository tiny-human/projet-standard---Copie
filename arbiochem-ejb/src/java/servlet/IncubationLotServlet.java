package servlet;

import bean.CGenUtil;
import ferme.configuration.Souche;
import ferme.lot.Lot;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/IncubationLotServlet")
public class IncubationLotServlet extends HttpServlet {

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String idLot = request.getParameter("idLot");

            if (idLot == null || idLot.trim().isEmpty()) {
                response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
                );

                out.print(
                    "{\"error\":\"Le lot est obligatoire.\"}"
                );
                return;
            }

            if (!idLot.matches("[A-Za-z0-9_-]+")) {
                response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
                );

                out.print(
                    "{\"error\":\"Identifiant de lot invalide.\"}"
                );
                return;
            }

            Lot[] lots = (Lot[])
                CGenUtil.rechercher(
                    new Lot(),
                    null,
                    null,
                    null,
                    " AND ID = '" + idLot + "'"
                );

            if (lots.length == 0) {
                response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
                );

                out.print(
                    "{\"error\":\"Lot introuvable.\"}"
                );
                return;
            }

            String idSouche = lots[0].getIdSouche();

            if (idSouche == null || idSouche.trim().isEmpty()) {
                response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
                );

                out.print(
                    "{\"error\":\"Le lot n'a pas de souche associée.\"}"
                );
                return;
            }

            if (!idSouche.matches("[A-Za-z0-9_-]+")) {
                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                out.print(
                    "{\"error\":\"Identifiant de souche invalide.\"}"
                );
                return;
            }

            Souche[] souches = (Souche[])
                CGenUtil.rechercher(
                    new Souche(),
                    null,
                    null,
                    null,
                    " AND ID = '" + idSouche + "'"
                );

            if (souches.length == 0) {
                response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
                );

                out.print(
                    "{\"error\":\"Souche introuvable.\"}"
                );
                return;
            }

            int incubation = souches[0].getIncubation();

            if (incubation <= 0) {
                response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
                );

                out.print(
                    "{\"error\":\"Durée d'incubation invalide.\"}"
                );
                return;
            }

            out.print(
                "{\"incubation\":" + incubation + "}"
            );
        } catch (Exception exception) {
            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            String message = exception.getMessage();

            if (message == null || message.trim().isEmpty()) {
                message = "Erreur serveur.";
            }

            message = message.replace("\"", "'");

            response.getWriter().print(
                "{\"error\":\"" + message + "\"}"
            );
        }
    }
}