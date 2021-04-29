package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class GameScreen implements Screen {
    final MyGdxGame game;

    Texture bergImage;
    Texture boatImage;
    Image bg;
    Sound bergSound;
    Music oceanMusic;
    //SpriteBatch batch;
    OrthographicCamera camera;
    Rectangle boat;
    Array<Rectangle> bergs;
    long lastbergTime;
    int bergsGathered;


    public GameScreen(final MyGdxGame game) {
        this.game = game;
        // load the images for the droplet and the bucket, 64x64 pixels each
        bergImage = new Texture(Gdx.files.internal("iceberg.png"));
        boatImage = new Texture(Gdx.files.internal("boat.png"));

        // load the drop sound effect and the rain background "music"
        bergSound = Gdx.audio.newSound(Gdx.files.internal("bergcrash.mp3"));
        oceanMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        oceanMusic.setLooping(true);


        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // create a Rectangle to logically represent the bucket
        boat = new Rectangle();
        boat.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        boat.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        boat.width = 94;
        boat.height = 70;

        // create the raindrops array and spawn the first raindrop
        bergs = new Array<Rectangle>();
        spawnBerg();
    }

    private void spawnBerg() {
        Rectangle berg = new Rectangle();
        berg.x = MathUtils.random(0, 800-64);
        berg.y = 480;
        berg.width = 32;
        berg.height = 32;
        bergs.add(berg);
        lastbergTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to clear are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        ScreenUtils.clear(0, 0, 0.2f, 1);
        URL urlBG = null;
        try {
            urlBG = new URL("http://pscode.org/media/stromlo2.jpg");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        bg = Toolkit.getDefaultToolkit().createImage(urlBG);


        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw the bucket and
        // all drops
        game.batch.begin();
        game.font.draw(game.batch, "Bergs hit: " + bergsGathered, 0, 480);
        game.batch.draw(boatImage, boat.x, boat.y, boat.width, boat.height);
        for (Rectangle raindrop : bergs) {
            game.batch.draw(bergImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        // process user input
        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            boat.x = touchPos.x - 64 / 2;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) boat.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) boat.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if(boat.x < 0) boat.x = 0;
        if(boat.x > 800 - 64) boat.x = 800 - 64;

        // check if we need to create a new raindrop
        if(TimeUtils.nanoTime() - lastbergTime > 1000000000) spawnBerg();

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the latter case we play back
        // a sound effect as well.
        for (Iterator<Rectangle> iter = bergs.iterator(); iter.hasNext(); ) {
            Rectangle bergs = iter.next();
            bergs.y -= 200 * Gdx.graphics.getDeltaTime();
            if(bergs.y + 64 < 0) iter.remove();
            if(bergs.overlaps(boat)) {
                bergsGathered++;
                bergSound.play();
                iter.remove();
            }
        }
        if (bergsGathered >= 5) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }
    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        oceanMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        // dispose of all the native resources
        bergImage.dispose();
        boatImage.dispose();
        bergSound.dispose();
        oceanMusic.dispose();
    }
}
