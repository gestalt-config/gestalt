import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Easy Multi-Source Config Management',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        Gestalt enables easy merging of configurations from multiple sources (files, environment variables, maps) into a unified structure, simplifying how you manage application settings.
      </>
    ),
  },
  {
    title: 'Type-Safe Automatic Decoding',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        It automatically decodes configurations into Java objects, including beans, records, and lists, supporting Java and Kotlin, while providing error feedback for missing or invalid configs.
      </>
    ),
  },
  {
    title: 'Modular and Lightweight',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        With zero core dependencies, Gestalt allows you to include only the necessary features, keeping your application lightweight and configurable for various environments or profiles.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
