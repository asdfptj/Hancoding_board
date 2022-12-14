package com.study.board.controller;

import com.study.board.entity.Board;
import com.study.board.repository.BoardRepository;
import com.study.board.service.BoardService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.System.out;


@Controller
public class BoardController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/excel/download")
    public void excelDownload(HttpServletResponse response, Integer id, Board board, @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) throws IOException {
//        Workbook wb = new HSSFWorkbook();
        Page<Board> list = null;
        list = boardService.boardList(pageable);
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("첫번째 시트");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;

        // Header
        row = sheet.createRow(rowNum++);
        cell = row.createCell(1);
        cell.setCellValue("글번호");
        cell = row.createCell(2);
        cell.setCellValue("제목");
//        cell = row.createCell(3);


        // Body
        for (int i = 0; i < list.getSize(); i++) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(1);
            cell.setCellValue(list.getContent().get(i).getId());
            cell = row.createCell(2);
            cell.setCellValue(list.getContent().get(i).getTitle());
//            cell = row.createCell(3);

        }

        // 컨텐츠 타입과 파일명 지정
        response.setContentType("ms-vnd/excel");
//        response.setHeader("Content-Disposition", "attachment;filename=example.xls");
        response.setHeader("Content-Disposition", "attachment;filename=example.xlsx");

        // Excel File Output
        wb.write(response.getOutputStream());
        wb.close();
    }



    @RequestMapping(value="/test.json", method=RequestMethod.POST)
    @ResponseBody
    public String simpleWithObject(@RequestParam("name") String name, @RequestParam("age") String age, @RequestParam("gender") String gender) {
        //필요한 로직 처리
        System.out.println(name);
        System.out.println(age);
        System.out.println(gender);
        return name+age+gender;
    }
    @GetMapping("/js")
    public String js() {
        return "jsproject";
    }

    @Autowired
    private BoardService boardService;
    @Autowired
    private BoardRepository boardRepository;

    @GetMapping("/board/write") //localhost:8090/board/write
    public String boardWriteForm() {

        return "boardwrite";
    }

    @PostMapping("/board/writepro")
    public String boardWritePro(Board board, Model model, MultipartFile file) throws Exception{

        boardService.write(board, file);

        model.addAttribute("message", "글 작성이 완료되었습니다.");
        model.addAttribute("searchUrl", "/board/list");

        return "message";
    }



    @GetMapping("/board/list")
    public String boardList(Model model,
                            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            String searchKeyword) {

        Page<Board> list = null;

        if(searchKeyword == null) {
            list = boardService.boardList(pageable);
        }else {
            list = boardService.boardSearchList(searchKeyword, pageable);
        }



        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());

        model.addAttribute("list", list);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        

        return "boardlist";


    }
    @GetMapping("/board/view") // localhost:8090/board/view?id=1
    public String boardView(Model model, Integer id) {

        model.addAttribute("board", boardService.boardView(id));
        return "boardview";
    }

//    @GetMapping("/board/delete")
//    public String boardDelete(Integer id, Model model) {
//        boardService.boardDelete(id);
//        model.addAttribute("message", "글 삭제가 완료되었습니다.");
//        model.addAttribute("searchUrl", "/board/list");
//        return "message";
//    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable("id") Integer id,
                              Model model) {

        model.addAttribute("board", boardService.boardView(id));

        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, MultipartFile file) throws Exception{

        Board boardTemp = boardService.boardView(id);
        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());

        boardService.write(boardTemp, file);

        return "redirect:/board/list";

    }

    @GetMapping("/board/deleteForm")
    public String deleteForm(Board board, HttpServletRequest request){
        String[] arrayParam = request.getParameterValues("nolmal");

        out.println("arrayParam : " + arrayParam[0]);
        for(int i=0; i<= arrayParam.length; i++){
            Board boardTemp = boardService.boardView(Integer.parseInt(arrayParam[i]));
            boardTemp.setDelYN("Y");
//            boardService.write(boardTemp);
        }
        return "redirect:/board/list";
    }

}
