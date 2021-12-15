import { EventEmitter, Injectable } from '@angular/core';
import { VideoMedia } from '../../basic/media/model/VideoMedia';
import { VideoControlDescription } from '../model/video/VideoControlDescription';
import { BehaviorSubject } from 'rxjs';

const FRAME_BARS_2_DISPLAY = 120;
const FRAME_NUMBERS_2_DISPLAY = 13;

const SKIP_FRAME_COUNTER = 10;
const DEFAULT_PLAYBACK_RATE = 0.25;


@Injectable({
  providedIn: 'root'
})
export class VideoControlService {

  /**
   * Some events to control the video within the workspace.
   */
  public readonly onSeek: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  public readonly onPlayPause: EventEmitter<boolean> = new EventEmitter<boolean>();
  public readonly onFrameChanged: EventEmitter<number> = new EventEmitter<number>();

  public readonly feDescription = new VideoControlDescription(FRAME_BARS_2_DISPLAY, FRAME_NUMBERS_2_DISPLAY);

  public readonly playbackRates: {id: number, label: string}[]= [
    {id: 0.10, label: '0.10'},
    {id: 0.15, label: '0.15'},
    {id: 0.20, label: '0.20'},
    {id: 0.25, label: '0.25'},
    {id: 0.5, label: '0.5'},
    {id: 0.75, label: '0.75'},
    {id: 1, label: 'FEATURE.WORKSPACE.VIDEO_SETTINGS.MODAL.NORMAL_SPEED'},
    {id: 1.25, label: '1.25'},
    {id: 1.5, label: '1.5'},
    {id: 1.75, label: '1.75'},
    {id: 2, label: '2'},
  ];

  public src = '';
  public fps = 0;
  public readonly startFrame = 0;
  public totalFrames = 0;
  public duration: number = 0;

  public get currentTime(): number {
    return this._currentTime;
  }

  /**
   * Called from the video element itself.
   *
   * @param newTime The ground truth about hte current time.
   */
  public set currentTime(newTime: number) {
    // The current time hook may be called twice for the same time stamp.
    if (this._currentTime !== newTime) {
      this._currentTime = newTime;
      this._currentFrameNumber = Math.floor(this.currentTime * this.fps);
      this.onFrameChanged.emit(this._currentFrameNumber);
    }
  }

  public loaded: boolean = false;
  public ended: boolean = false;

  public get playbackRate(): number {
    return this._playbackRate;
  }

  public set playbackRate(rate: number) {
    if (rate !== this._playbackRate) {
      const rates = this.playbackRates.map(r => r.id);
      if (rates.includes(rate)) {
        this._playbackRate = rate;
      }
    }
  }

  /**
   * The number of frames to skip when using the
   * - fast forward button
   * - fast backward button
   * - shortcut 'j'
   * - shortcut 'l'
   */
  public readonly skipFrameCounter = SKIP_FRAME_COUNTER;

  /*
   * Is playing / set playing state.
   */
  public get isPlaying(): boolean {
    return this._isPlaying;
  }

  public set isPlaying(playing: boolean) {
    if (playing !== this._isPlaying) {
      this._isPlaying = playing;
      this.onPlayPause.emit(playing);
    }
  }

  public get currentFrameNumber(): number {
    return Math.floor(this.currentTime * this.fps);
  }

  public set currentFrameNumber(nextFrame: number) {
    const offset = nextFrame - this.currentFrameNumber;
    this.isPlaying = false;
    this.seekFrame(offset);
  }

  /*
   * The speed of the video playback.
   */
  private _playbackRate = DEFAULT_PLAYBACK_RATE;
  private _isPlaying: boolean = false;
  private _currentFrameNumber = 0;
  private _currentTime: number = 0;

  constructor() {}

  public init(video: VideoMedia, frameNumber: number): void {
    this.reset();

    this.fps = video.fps;
    this.src = video.url;
    this.totalFrames = video.totalFrames;

    // If possible, jump to the given frameNumber.
    this.seekFrame(frameNumber);
  }

  public reset(): void {
    this.fps = 0;
    this.src = '';
    this.totalFrames = 0;
    // No autoplay
    this.isPlaying = false;

    this._playbackRate = DEFAULT_PLAYBACK_RATE;
    this._currentFrameNumber = 0;
    this._currentTime = 0;
  }

  public speedUp(): void {
    const current = this.playbackRate;
    const rates = this.playbackRates.map(r => r.id);
    const next = rates.filter(r => r > current);
    if (next.length > 0) {
      this.playbackRate = Math.min(...next);
    }
  }

  public slowDown(): void {
    const current = this.playbackRate;
    const rates = this.playbackRates.map(r => r.id);
    const next = rates.filter(r => r < current);
    if (next.length > 0) {
      this.playbackRate = Math.max(...next);
    }
  }

  /**
   * Is at the current index a possible frame?
   * @param offset: e.g. -1 for backward one frame.
   */
  public frameSkipPossible(offset: number): boolean {
    const newFrame = this.currentFrameNumber + offset;

    return 0 <= newFrame && newFrame <= this.totalFrames;
  }

  public seekFrame(offset: number): void {

    if (!this.frameSkipPossible(offset)) {
      // Have some break. Here is nothing to do.
      return;
    }

    // Adding the crop factor to avoid "back frame jumping"
    const fpsFactor = 0.001;

    const newFrame = this._currentFrameNumber + offset;
    const newPos = (newFrame / this.fps) + fpsFactor;

    this.isPlaying = false;
    /*
     * Do not set the `this._currentTime` flag direct.
     * The setter method emits the `onFrameChanged` event that updates
     * the video control panel.
     */
    this.currentTime = newPos;
    this.onSeek.next(newPos);
  }
}
