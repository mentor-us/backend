package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserProfile;
import com.hcmus.mentor.backend.controller.usecase.grade.getgrade.SearchGradeQuery;
import com.hcmus.mentor.backend.domain.Grade;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.GradeRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = "gradeCache")
public class GradeService {

    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public Page<Grade> searchGrade(SearchGradeQuery query) {
        return gradeRepository.search(query);
    }

    public Optional<Grade> findById(String id) {
        return gradeRepository.findById(id);
    }

    public Grade create(Grade grade) {
        return gradeRepository.save(grade);
    }

    public Grade update(Grade grade) {
        return gradeRepository.save(grade);
    }

    public void delete(Grade grade) {
        gradeRepository.delete(grade);
    }

    public boolean canAccessGrade(String gradeId, String viewerId) {
        return gradeRepository.canAccessGrade(gradeId, viewerId);
    }

    public boolean canAccessUser(String userId, String viewerId) {
        return userRepository.canAccessUserGrade(userId, viewerId);
    }

    public GradeUserDto mapToGradeUserDto(User user) {
        var result = modelMapper.map(user, GradeUserDto.class);
        var userAccesses = user.getUserCanAccessGrade().stream()
                .map(userAccess -> modelMapper.map(userAccess.getUserAccess(), GradeUserProfile.class)).toList();
        result.setUserAccesses(userAccesses);

        return result;
    }
}