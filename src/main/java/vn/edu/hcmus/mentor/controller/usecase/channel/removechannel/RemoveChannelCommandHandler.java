package vn.edu.hcmus.mentor.controller.usecase.channel.removechannel;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.domain.AuditRecord;
import vn.edu.hcmus.mentor.domain.Message;
import vn.edu.hcmus.mentor.domain.constant.ActionType;
import vn.edu.hcmus.mentor.domain.constant.ChannelStatus;
import vn.edu.hcmus.mentor.domain.constant.DomainType;
import vn.edu.hcmus.mentor.repository.ChannelRepository;
import vn.edu.hcmus.mentor.repository.MessageRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.AuditRecordService;
import vn.edu.hcmus.mentor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for {@link RemoveChannelCommand}.
 */
@Component
@RequiredArgsConstructor
public class RemoveChannelCommandHandler implements Command.Handler<RemoveChannelCommand, Voidy> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final PermissionService permissionService;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Voidy handle(RemoveChannelCommand command) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(command.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        if (channel.getStatus().equals(ChannelStatus.DELETED)) {
            throw new DomainException("Kênh đã bị xoá");
        }
        var group = channel.getGroup();

        if (!permissionService.isMentorInChannel(command.getId(), userId)) {
            throw new ForbiddenException("Không có quyền xoá kênh");
        }

        if (group.getDefaultChannel().getId().equals(channel.getId())) {
            throw new DomainException("Không thể xoá kênh mặc định");
        }

        messageRepository.deleteAllByChannelId(channel.getId(), Message.Status.DELETED);
        channel.setStatus(ChannelStatus.DELETED);
        channelRepository.save(channel);

        auditRecordService.save(AuditRecord.builder()
                .user(userRepository.findById(userId).orElse(null))
                .entityId(channel.getId())
                .domain(DomainType.CHANNEL)
                .action(ActionType.DELETED)
                .detail(String.format("Xoá kênh %s", channel.getName()))
                .build());

        return null;
    }
}