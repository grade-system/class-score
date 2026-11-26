package com.example.servlet;

import com.example.entity.ScoreRecord;
import com.example.service.ScoreRecordService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 成绩管理控制层：列表与统计、录入成绩、修改成绩（留痕）、查看修改记录
 */
@WebServlet("/scores")
public class ScoreRecordServlet extends HttpServlet {

    private final ScoreRecordService scoreRecordService = new ScoreRecordService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("edit".equals(action)) {
            showEdit(request, response);
            return;
        }
        if ("logs".equals(action)) {
            String studentNo = trim(request.getParameter("studentNo"));
            request.setAttribute("logs", scoreRecordService.recentLogs(50, studentNo));
            request.setAttribute("studentNo", studentNo);
            request.getRequestDispatcher("logs.jsp").forward(request, response);
            return;
        }
        String subject = trim(request.getParameter("subject"));
        String keyword = trim(request.getParameter("kw"));
        int pageNo = parsePage(request.getParameter("page"));
        double[] summary = scoreRecordService.summary(subject, keyword);
        int pages = scoreRecordService.totalPages(summary[0]);
        if (pageNo > pages) {
            pageNo = pages;
        }
        List<ScoreRecord> list = scoreRecordService.search(subject, keyword, pageNo);
        request.setAttribute("scoreList", list);
        request.setAttribute("subjects", scoreRecordService.subjects());
        request.setAttribute("subject", subject);
        request.setAttribute("kw", keyword);
        request.setAttribute("total", (int) summary[0]);
        request.setAttribute("pageNo", pageNo);
        request.setAttribute("pages", pages);
        request.setAttribute("average", String.format("%.1f", summary[1]));
        request.setAttribute("passRate", String.format("%.1f", summary[2]));
        request.getRequestDispatcher("list.jsp").forward(request, response);
    }

    private static int parsePage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (Exception e) {
            return 1;
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("update".equals(request.getParameter("action"))) {
            updateScore(request, response);
            return;
        }
        ScoreRecord record = new ScoreRecord();
        record.setStudentNo(request.getParameter("studentNo"));
        record.setStudentName(request.getParameter("studentName"));
        record.setClassName(request.getParameter("className"));
        record.setSubject(request.getParameter("subject"));
        try {
            record.setScore(Double.valueOf(request.getParameter("score")));
            scoreRecordService.add(record);
            response.sendRedirect(request.getContextPath() + "/scores?ok=1");
        } catch (NumberFormatException e) {
            forwardWithError(request, response, "分数必须是数字");
        } catch (IllegalArgumentException e) {
            forwardWithError(request, response, e.getMessage());
        }
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            ScoreRecord record = scoreRecordService.get(id);
            if (record == null) {
                response.sendRedirect(request.getContextPath() + "/scores");
                return;
            }
            request.setAttribute("record", record);
            request.getRequestDispatcher("edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/scores");
        }
    }

    private void updateScore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/scores");
            return;
        }
        String operator = request.getParameter("operator");
        try {
            double newScore = Double.parseDouble(request.getParameter("score"));
            scoreRecordService.updateScore(id, newScore, operator);
            response.sendRedirect(request.getContextPath() + "/scores?updated=1");
        } catch (NumberFormatException e) {
            forwardEditError(request, response, id, "分数必须是数字");
        } catch (IllegalArgumentException e) {
            forwardEditError(request, response, id, e.getMessage());
        }
    }

    private void forwardEditError(HttpServletRequest request, HttpServletResponse response,
                                  int id, String message)
            throws ServletException, IOException {
        ScoreRecord record = scoreRecordService.get(id);
        request.setAttribute("record", record);
        request.setAttribute("error", message);
        request.getRequestDispatcher("edit.jsp").forward(request, response);
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("add.jsp").forward(request, response);
    }
}
