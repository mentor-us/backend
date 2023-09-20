package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.FAQ;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.payload.response.FAQDetail;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Tag(name = "FAQ APIs", description = "REST APIs for FAQ collections")
@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @Operation(summary = "Get all FAQs of group",
            description = "Get all FAQs by group ID", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @GetMapping(value = {"/", ""})
    public ResponseEntity<List<FAQ>> all(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                         @RequestParam String groupId) {
        List<FAQ> faqs = faqService.getByGroupId(userPrincipal.getId(), groupId);
        return ResponseEntity.ok(faqs);
    }

    @Operation(summary = "Get FAQ detail",
            description = "Get existing FAQ detail in group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @GetMapping("/{faqId}")
    public ResponseEntity<FAQDetail> get(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                         @PathVariable String faqId) {
        FAQDetail faq = faqService.getById(userPrincipal.getId(), faqId);
        if (faq == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faq);
    }

    @Operation(summary = "Add FAQ",
            description = "Add FAQ to group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @PostMapping(value = {"/", ""})
    public ResponseEntity<String> create(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                      @RequestBody CreateFaqRequest request) {
        FAQ faq = faqService.addNewFaq(userPrincipal.getId(), request);
        if (faq == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(faq.getId());
    }

    @Operation(summary = "Update FAQ",
            description = "Update existing FAQ in group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @PatchMapping("/{faqId}")
    public ResponseEntity<String> update(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                         @PathVariable String faqId,
                                         @RequestBody UpdateFaqRequest request) {
        FAQ faq = faqService.updateFAQ(userPrincipal.getId(), faqId, request);
        if (faq == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(faq.getId());
    }

    @Operation(summary = "Delete FAQ",
            description = "Delete existing FAQ in group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @DeleteMapping("/{faqId}")
    public ResponseEntity<String> delete(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                         @PathVariable String faqId) {
        boolean isDeleted = faqService.deleteFaq(userPrincipal.getId(), faqId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Import FAQs",
            description = "Import existing FAQs from another group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @PostMapping("/{faqId}/import")
    public ResponseEntity<String> importFAQs(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                             @PathVariable String faqId,
                                             @RequestBody ImportFAQsRequest request) {
        faqService.importFAQs(userPrincipal, faqId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Upvote FAQ",
            description = "Upvote existing FAQ in group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upvote successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @PostMapping("/{faqId}/upvote")
    public ResponseEntity<String> upvote(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                         @PathVariable String faqId) {
        boolean isSuccess = faqService.upvote(userPrincipal, faqId);
        if (!isSuccess) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Downvote FAQ",
            description = "Downvote existing FAQ in group", tags = "FAQ APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upvote successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class)))
            )})
    @PostMapping("/{faqId}/downVote")
    public ResponseEntity<String> downVote(@ApiIgnore @CurrentUser UserPrincipal userPrincipal,
                                         @PathVariable String faqId) {
        boolean isSuccess = faqService.downVote(userPrincipal, faqId);
        if (!isSuccess) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
