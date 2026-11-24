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
 * 成绩管理控制层：GET 列表与统计，POST 录入成绩
 */
@WebServlet("/scores")
public class ScoreRecordServlet extends HttpServlet {

    private final ScoreRecordService scoreRecordService = new ScoreRecordService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ScoreRecord> list = scoreRecordService.listAll();
        request.setAttribute("scoreList", list);
        request.setAttribute("average", String.format("%.1f", scoreRecordService.average(list)));
        request.setAttribute("passRate", String.format("%.1f", scoreRecordService.passRate(list)));
        request.getRequestDispatcher("list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("add.jsp").forward(request, response);
    }
}
