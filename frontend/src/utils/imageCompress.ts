interface CompressOptions {
  maxWidth?: number;
  maxHeight?: number;
  quality?: number;
  maxSize?: number;
}

export async function compressImage(file: File, options: CompressOptions = {}): Promise<Blob> {
  const {
    maxWidth = 1200,
    maxHeight = 1200,
    quality = 0.6,
    maxSize = 200 * 1024
  } = options;

  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let { width, height } = img;

        if (width > maxWidth || height > maxHeight) {
          const ratio = Math.min(maxWidth / width, maxHeight / height);
          width = Math.round(width * ratio);
          height = Math.round(height * ratio);
        }

        canvas.width = width;
        canvas.height = height;

        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('Failed to get canvas context'));
          return;
        }

        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, width, height);
        ctx.drawImage(img, 0, 0, width, height);

        let currentQuality = quality;
        const compress = () => {
          canvas.toBlob(
            (blob) => {
              if (blob) {
                if (blob.size > maxSize && currentQuality > 0.2) {
                  currentQuality = Math.max(0.2, currentQuality - 0.1);
                  compress();
                } else if (blob.size > maxSize && width > 400) {
                  const newRatio = Math.max(400 / width, 0.7);
                  width = Math.round(width * newRatio);
                  height = Math.round(height * newRatio);
                  canvas.width = width;
                  canvas.height = height;
                  ctx.fillStyle = '#ffffff';
                  ctx.fillRect(0, 0, width, height);
                  ctx.drawImage(img, 0, 0, width, height);
                  currentQuality = 0.5;
                  compress();
                } else {
                  resolve(blob);
                }
              } else {
                reject(new Error('Failed to compress image'));
              }
            },
            'image/jpeg',
            currentQuality
          );
        };
        compress();
      };
      img.onerror = () => reject(new Error('Failed to load image'));
      img.src = e.target?.result as string;
    };
    reader.onerror = () => reject(new Error('Failed to read file'));
    reader.readAsDataURL(file);
  });
}

export async function compressAvatar(file: File): Promise<Blob> {
  return compressImage(file, {
    maxWidth: 256,
    maxHeight: 256,
    quality: 0.6,
    maxSize: 50 * 1024
  });
}

export async function compressGiftImage(file: File): Promise<Blob> {
  return compressImage(file, {
    maxWidth: 200,
    maxHeight: 200,
    quality: 0.5,
    maxSize: 30 * 1024
  });
}
