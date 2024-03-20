package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.request.faqs.CreateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.ImportFAQsRequest;
import com.hcmus.mentor.backend.controller.payload.request.faqs.UpdateFaqRequest;
import com.hcmus.mentor.backend.controller.payload.response.FAQDetail;
import com.hcmus.mentor.backend.domain.Faq;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.FaqService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@Tag(name = "faqs")
@RestController
@RequestMapping("api/faqs")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    /**
     * Retrieves all FAQs of a group.
     *
     * @param loggedUser    The current user's principal information.
     * @param groupId       The ID of the group to retrieve FAQs.
     * @return ResponseEntity containing a list of FAQs for the specified group.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<List<Faq>> all(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedUser,
            @RequestParam String groupId) {
        List<Faq> faqs = faqService.getByGroupId(loggedUser.getId(), groupId);

        return ResponseEntity.ok(faqs);
    }

    /**
     * Retrieves the details of a specific FAQ.
     *
     * @param customerUserDetails The current user's principal information.
     * @param faqId         The ID of the FAQ to retrieve details.
     * @return ResponseEntity containing the detailed information of the specified FAQ.
     */
    @GetMapping("{faqId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<FAQDetail> get(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String faqId) {
        FAQDetail faq = faqService.getById(customerUserDetails.getId(), faqId);
        if (faq == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faq);
    }

    /**
     * Adds a new FAQ to a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param request       The request containing information to create a new FAQ.
     * @return ResponseEntity containing the ID of the newly created FAQ.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> create(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody CreateFaqRequest request) {
        Faq faq = faqService.createFaq(customerUserDetails.getId(), request);
        if (faq == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(faq.getId());
    }

    /**
     * Updates an existing FAQ in a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param faqId         The ID of the FAQ to be updated.
     * @param request       The request containing updated information for the FAQ.
     * @return ResponseEntity containing the ID of the updated FAQ.
     */
    @PatchMapping("{faqId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> update(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String faqId,
            @RequestBody UpdateFaqRequest request) {
        Faq faq = faqService.updateFAQ(customerUserDetails.getId(), faqId, request);
        if (faq == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(faq.getId());
    }

    /**
     * Deletes an existing FAQ from a group.
     *
     * @param loggedUser    The current user's principal information.
     * @param faqId         The ID of the FAQ to be deleted.
     * @return ResponseEntity indicating the success of the deletion operation.
     */
    @DeleteMapping("{faqId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> delete(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedUser,
            @PathVariable String faqId) {
        faqService.deleteFaq(loggedUser.getId(), faqId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Imports existing FAQs from another group.
     *
     * @param loggedUser    The current user's principal information.
     * @param destGroupId   The ID of the FAQ to which FAQs will be imported.
     * @param request       The request containing information about the FAQs to import.
     * @return ResponseEntity indicating the success of the import operation.
     */
    @PostMapping("{groupIdgroupId}/import")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> importFAQs(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails loggedUser,
            @PathVariable String destGroupId,
            @RequestBody ImportFAQsRequest request) {
        faqService.importFaqs(loggedUser.getId(), destGroupId, request);

        return ResponseEntity.ok().build();
    }

    /**
     * Upvotes an existing FAQ in a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param faqId         The ID of the FAQ to be upvoted.
     * @return ResponseEntity indicating the success of the upvote operation.
     */
    @PostMapping("{faqId}/upvote")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> upVote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String faqId) {
        boolean isSuccess = faqService.upvote(customerUserDetails, faqId);
        if (!isSuccess) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Downvotes an existing FAQ in a group.
     *
     * @param customerUserDetails The current user's principal information.
     * @param faqId         The ID of the FAQ to be downvoted.
     * @return ResponseEntity indicating the success of the downvote operation.
     */
    @PostMapping("{faqId}/downVote")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<String> downVote(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String faqId) {
        boolean isSuccess = faqService.downVote(customerUserDetails, faqId);
        if (!isSuccess) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
