package vn.edu.hcmus.mentor.controller.usecase.note.common;

public interface NoteUserProfileProjection {
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
    Long getTotalNotes();
}