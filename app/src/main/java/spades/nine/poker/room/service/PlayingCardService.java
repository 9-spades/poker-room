package spades.nine.poker.room.service;

import java.util.List;
import java.util.UUID;

import spades.nine.poker.room.entity.PlayingCardEntity;
import spades.nine.poker.room.model.PlayingCard;
import spades.nine.poker.room.repository.PlayingCardRepository;

public class PlayingCardService {
    private final PlayingCardRepository repository;

    public PlayingCardService() {
        this(new PlayingCardRepository());
    }

    public PlayingCardService(PlayingCardRepository repository) {
        this.repository = repository;
    }

    public List<PlayingCardEntity> getAllItems() {
        return repository.findAll();
    }

    public PlayingCardEntity createItem(PlayingCard item) {
        if(item == null) throw new IllegalArgumentException();
        PlayingCardEntity playingCardEntity = new PlayingCardEntity();
        playingCardEntity.setId(generateUniqueId(item));
        playingCardEntity.setHeading(item.getHeading());
        playingCardEntity.setLabel(item.getLabel());
        playingCardEntity.setSublabel(item.getSublabel());
        playingCardEntity.setContent(item.getContent());
        return repository.save(playingCardEntity);
    }

    private UUID generateUniqueId(PlayingCard item) {
        UUID id;
        int collisions = 0;
        do {
            id = UUID.nameUUIDFromBytes(Integer.toString(item.hashCode()+collisions).getBytes());
            collisions++;
        } while(repository.existsById(id));
        return id;
    }

    public boolean deleteItem(UUID id) {
        return repository.deleteById(id);
    }
}
