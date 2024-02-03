package fr.univlille.utils;

public interface Observer {
        public void update(Subject subj);

        public void update(Subject subj, Object data);
}
