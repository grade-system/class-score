package com.example.servlet;

import com.example.entity.SubjectStat;
import com.example.service.ScoreRecordService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 科目统计页：按科目聚合平均分、最高分、最低分与及格率
 */
@WebServlet("/stats")
public class StatsServlet extends HttpServlet {

    private final ScoreRecordService scoreRecordService = new ScoreRecordService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<SubjectStat> stats = scoreRecordService.subjectStats();
        request.setAttribute("stats", stats);
        request.getRequestDispatcher("stats.jsp").forward(request, response);
    }
}
